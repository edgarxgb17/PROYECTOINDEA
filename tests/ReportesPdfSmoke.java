package com.indea.util;

import com.indea.util.ReportePdf.Columna;
import com.indea.util.ReportePdf.Informe;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ReportesPdfSmoke {
    public static void main(String[] args) throws Exception {
        var filas = new ArrayList<List<String>>();
        for (int i=1; i<=80; i++) {
            filas.add(List.of("INDEA-OT-"+(4010+i),"Laptop Lenovo ThinkPad T480 edición especial",
                    "En diagnóstico", "18/11/2026 17:20","Óscar Hernández (turno 2)","Pendiente"));
        }
        var dir=Path.of(args[0]);
        ReportePdf.guardar(dir.resolve("seguimiento.pdf"),new Informe("Seguimiento de reparaciones",
                "Todas las reparaciones del sistema",List.of("80 folios · revisión del diseño y paginación."),
                List.of(new Columna("Folio",104),new Columna("Equipo",100),new Columna("Estado",91),
                        new Columna("Ingreso",83),new Columna("Técnico",95),new Columna("Pago",59)),filas));
        ReportePdf.guardar(dir.resolve("ganancias.pdf"),new Informe("Ganancias y cobros","Periodo de prueba · MXN",
                List.of("No es utilidad neta. No descuenta mano de obra ni impuestos."),
                List.of(new Columna("Concepto",374),new Columna("Importe o cantidad",158)),
                List.of(List.of("Ingresos por ventas","$19,750.00"),List.of("Costo registrado","$12,500.00"),
                        List.of("Margen bruto calculable","$7,250.00"))));
        ReportePdf.guardar(dir.resolve("inventario.pdf"),new Informe("Inventario de equipos","Estado consultado: Todos",
                List.of("1 equipo · 1 disponible."),
                List.of(new Columna("ID",48),new Columna("Equipo",177),new Columna("Condición",111),
                        new Columna("Precio de venta",96),new Columna("Estado",100)),
                List.of(List.of("1","Laptop Dell Latitude 5410","Reacondicionada","$4,690.00","Disponible"))));
        ReportePdf.guardar(dir.resolve("refacciones.pdf"),new Informe("Existencias de refacciones","Almacén · MXN",
                List.of(),List.of(new Columna("Refacción",230),new Columna("Existencias",78),
                        new Columna("Costo unitario",107),new Columna("Valor en almacén",117)),List.of()));
    }
}
