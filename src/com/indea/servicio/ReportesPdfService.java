package com.indea.servicio;

import com.indea.excepcion.PersistenciaException;
import com.indea.modelo.EstadoReparacion;
import com.indea.modelo.Reparacion;
import com.indea.modelo.Rol;
import com.indea.util.ConexionBD;
import com.indea.util.ReportePdf.Columna;
import com.indea.util.ReportePdf.Informe;
import com.indea.util.Sesion;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.text.NumberFormat;
import java.util.*;

public final class ReportesPdfService {
    private static final DateTimeFormatter DIA=DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter MOMENTO=DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Locale MX=Locale.forLanguageTag("es-MX");
    private ReportesPdfService() {}

    public static Informe ganancias(LocalDate desde,LocalDate hasta) {
        Sesion.exigirRol(Rol.ADMINISTRADOR);
        if (hasta.isBefore(desde)) throw new IllegalArgumentException("La fecha final debe ser igual o posterior a la inicial.");
        String ventas = """
                SELECT COUNT(*), COALESCE(SUM(v.precio_venta),0),
                       COALESCE(SUM(c.precio_compra),0),
                       COALESCE(SUM(CASE WHEN c.id_compra IS NULL THEN 0 ELSE v.precio_venta END),0),
                       COUNT(*)-COUNT(c.id_compra)
                FROM ventas v LEFT JOIN compras c ON c.id_equipo=v.id_equipo
                WHERE v.fecha_venta>=? AND v.fecha_venta<?
                """;
        String servicios = """
                SELECT COUNT(*),COALESCE(SUM(r.costo_servicio),0),COALESCE(SUM(p.piezas),0)
                FROM reparaciones r LEFT JOIN (
                    SELECT id_reparacion,SUM(cantidad*costo_unitario) piezas
                    FROM reparacion_refaccion GROUP BY id_reparacion
                ) p ON r.id_reparacion=p.id_reparacion
                WHERE r.pagado=1 AND r.fecha_pago>=? AND r.fecha_pago<?
                """;
        try (Connection c=ConexionBD.obtener()) {
            c.setReadOnly(true);
            c.setAutoCommit(false);
            List<List<String>> filas=new ArrayList<>();
            BigDecimal ventaTotal, margenVenta, cobros, margenServicio;
            long faltantes;
            try (PreparedStatement p=c.prepareStatement(ventas);ResultSet r=periodo(p,desde,hasta)) {
                r.next();
                ventaTotal=r.getBigDecimal(2);
                margenVenta=r.getBigDecimal(4).subtract(r.getBigDecimal(3));
                faltantes=r.getLong(5);
                filas.add(List.of("Equipos vendidos",Long.toString(r.getLong(1))));
                filas.add(List.of("Ingresos por ventas",dinero(ventaTotal)));
                filas.add(List.of("Costo conocido de equipos vendidos",dinero(r.getBigDecimal(3))));
                filas.add(List.of("Margen de ventas con costo registrado",dinero(margenVenta)));
            }
            try (PreparedStatement p=c.prepareStatement(servicios);ResultSet r=periodo(p,desde,hasta)) {
                r.next();
                cobros=r.getBigDecimal(2);
                margenServicio=cobros.subtract(r.getBigDecimal(3));
                filas.add(List.of("Reparaciones cobradas",Long.toString(r.getLong(1))));
                filas.add(List.of("Ingresos por reparaciones cobradas",dinero(cobros)));
                filas.add(List.of("Costo registrado de refacciones utilizadas",dinero(r.getBigDecimal(3))));
                filas.add(List.of("Margen de reparaciones después de refacciones",dinero(margenServicio)));
            }
            filas.add(List.of("Ingresos registrados del periodo",dinero(ventaTotal.add(cobros))));
            filas.add(List.of("Margen bruto calculable",dinero(margenVenta.add(margenServicio))));
            c.commit();
            List<String> notas=new ArrayList<>();
            notas.add("No es utilidad neta. No descuenta mano de obra, gastos generales ni impuestos.");
            notas.add("Para reparaciones se usa la fecha de cobro. Los pagos sin fecha registrada no entran en el periodo.");
            if (faltantes>0) notas.add(faltantes+" venta(s) sin costo de compra se cuentan en ingresos, pero no en el margen.");
            return new Informe("Ganancias y cobros", "Periodo del "+desde.format(DIA)+" al "+hasta.format(DIA)+" · MXN",
                    notas,List.of(col("Concepto",374),col("Importe o cantidad",158)),filas);
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo calcular el PDF de ganancias. "+e.getMessage(),e);
        }
    }

    private static ResultSet periodo(PreparedStatement p,LocalDate desde,LocalDate hasta) throws SQLException {
        p.setObject(1,desde);
        p.setObject(2,hasta.plusDays(1));
        return p.executeQuery();
    }

    public static Informe inventario(String filtro) {
        Sesion.exigirRol(Rol.values());
        Object[][] datos=new NegocioService().query("inventario",filtro,filtro);
        List<List<String>> filas=new ArrayList<>();
        int disponibles=0,revision=0,vendidos=0;
        for (Object[] d : datos) {
            String estado=txt(d[6]);
            switch(estado) {
                case "DISPONIBLE" -> disponibles++;
                case "NO_DISPONIBLE_AUN" -> revision++;
                case "VENDIDO" -> vendidos++;
            }
            String equipo=String.join(" ",txt(d[1]),txt(d[2]),txt(d[3])).strip().replaceAll(" +"," ");
            filas.add(List.of(txt(d[0]),equipo,txt(d[4]),dinero((BigDecimal)d[5]),estadoEquipo(estado)));
        }
        return new Informe("Inventario de equipos","Estado consultado: "+(filtro.isBlank()?"Todos":estadoEquipo(filtro)),
                List.of(datos.length+" equipo(s) · "+disponibles+" disponibles · "+revision+" no disponibles aún · "+vendidos+" vendidos.",
                        "Cada unidad del inventario aparece por separado. Se muestra el precio de venta registrado."),
                List.of(col("ID",48),col("Equipo",177),col("Condición",111),col("Precio de venta",96),col("Estado",100)),filas);
    }

    public static Informe reparaciones(List<Reparacion> lista,boolean filtrada) {
        Sesion.exigirRol(Rol.ADMINISTRADOR,Rol.TECNICO);
        List<List<String>> filas=new ArrayList<>();
        long entregadas=0, listas=0, pendientesPago=0;
        for (Reparacion r:lista) {
            if (r.getEstado()==EstadoReparacion.ENTREGADO) entregadas++;
            if (r.getEstado()==EstadoReparacion.LISTO_PARA_ENTREGA) listas++;
            if (!r.isPagado() && r.getCostoServicio().signum()>0) pendientesPago++;
            filas.add(List.of(r.getFolio(),r.getEquipoDescripcion(),r.getEstado().getEtiqueta(),
                    r.getFechaIngreso().format(MOMENTO),r.getTecnico()==null?"":r.getTecnico().getNombre(),
                    r.isPagado()?"Pagado":r.getCostoServicio().signum()>0?"Pendiente":"Sin costo"));
        }
        return new Informe("Seguimiento de reparaciones",filtrada?"Reparaciones de la búsqueda aplicada":"Todas las reparaciones del sistema",
                List.of(lista.size()+" folio(s) · "+entregadas+" entregados · "+listas+" listos para entrega · "+pendientesPago+" con pago pendiente.",
                        "Incluye folios, equipos y técnicos. No imprime los datos de contacto de los clientes."),
                List.of(col("Folio",104),col("Equipo",100),col("Estado",91),col("Ingreso",83),col("Técnico",95),col("Pago",59)),filas);
    }

    public static Informe refacciones() {
        List<RefaccionService.Refaccion> piezas=new ArrayList<>(new RefaccionService().listar());
        piezas.sort(Comparator.comparingInt(RefaccionService.Refaccion::stock).thenComparing(RefaccionService.Refaccion::nombre));
        List<List<String>> filas=new ArrayList<>();
        long unidades=0, agotadas=0;
        BigDecimal valor=BigDecimal.ZERO;
        for (var p:piezas) {
            BigDecimal importe=p.costo().multiply(BigDecimal.valueOf(p.stock()));
            unidades+=p.stock();
            if (p.stock()==0) agotadas++;
            valor=valor.add(importe);
            filas.add(List.of(p.nombre(),Integer.toString(p.stock()),dinero(p.costo()),dinero(importe)));
        }
        return new Informe("Existencias de refacciones","Almacén de refacciones · MXN",
                List.of(piezas.size()+" tipo(s) de pieza · "+unidades+" unidad(es) · "+agotadas+" sin existencias.",
                        "Valor de existencias al costo registrado: "+dinero(valor)+". Las piezas agotadas aparecen primero."),
                List.of(col("Refacción",230),col("Existencias",78),col("Costo unitario",107),col("Valor en almacén",117)),filas);
    }

    static String estadoEquipo(String s) {
        return switch(s) {
            case "DISPONIBLE" -> "Disponible";
            case "VENDIDO" -> "Vendido";
            case "NO_DISPONIBLE_AUN" -> "No disponible aún";
            default -> s;
        };
    }
    private static String txt(Object o) { return Objects.toString(o,""); }
    private static Columna col(String t,int a) { return new Columna(t,a); }
    private static String dinero(BigDecimal v) { return NumberFormat.getCurrencyInstance(MX).format(v); }
}
