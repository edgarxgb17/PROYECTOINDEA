package com.indea.servicio;

import com.indea.excepcion.PersistenciaException;
import com.indea.excepcion.ReglaNegocioException;
import com.indea.excepcion.ValidacionException;
import com.indea.modelo.Rol;
import com.indea.util.ConexionBD;
import com.indea.util.Sesion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Catálogo de piezas y consumo de existencias por reparación. */
public final class RefaccionService {
    public record Refaccion(int id, String nombre, BigDecimal costo, int stock) {}
    public record Aplicada(String nombre, int cantidad, BigDecimal costoUnitario) {}

    public List<Refaccion> listar() {
        Sesion.exigirRol(Rol.ADMINISTRADOR, Rol.TECNICO);
        String sql = "SELECT id_refaccion, nombre, costo, stock FROM refacciones ORDER BY nombre";
        try (Connection c = ConexionBD.obtener(); PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            List<Refaccion> lista = new ArrayList<>();
            while (r.next()) lista.add(new Refaccion(r.getInt(1), r.getString(2), r.getBigDecimal(3), r.getInt(4)));
            return lista;
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo consultar el catálogo de refacciones.", e);
        }
    }

    public void registrar(String nombre, String costo, String stockInicial) {
        Sesion.exigirRol(Rol.ADMINISTRADOR);
        String pieza = nombre == null ? "" : nombre.strip();
        if (pieza.isEmpty() || pieza.length() > 100) {
            throw new ValidacionException("Escribe un nombre de hasta 100 caracteres.");
        }
        BigDecimal importe = validarCosto(costo);
        int stock = validarCantidad(stockInicial, true);
        try (Connection c = ConexionBD.obtener();
             PreparedStatement p = c.prepareStatement("INSERT INTO refacciones (nombre, costo, stock) VALUES (?, ?, ?)")) {
            p.setString(1, pieza);
            p.setBigDecimal(2, importe);
            p.setInt(3, stock);
            p.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new ReglaNegocioException("Ya existe una refacción con ese nombre. Selecciónala para agregar existencias.");
            }
            throw new PersistenciaException("No se pudo guardar la refacción.", e);
        }
    }

    public void reponer(int idRefaccion, String unidades) {
        Sesion.exigirRol(Rol.ADMINISTRADOR);
        int cantidad = validarCantidad(unidades, false);
        String sql = "UPDATE refacciones SET stock = stock + ? WHERE id_refaccion = ? AND stock <= ?";
        try (Connection c = ConexionBD.obtener(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, cantidad);
            p.setInt(2, idRefaccion);
            p.setInt(3, Integer.MAX_VALUE - cantidad);
            if (p.executeUpdate() != 1) throw new ReglaNegocioException("No se pudo aumentar la existencia de esa pieza.");
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo registrar la entrada de refacciones.", e);
        }
    }

    public List<Aplicada> listarPorFolio(String folio) {
        Sesion.exigirRol(Rol.ADMINISTRADOR, Rol.TECNICO);
        String sql = """
                SELECT f.nombre, rr.cantidad, rr.costo_unitario
                FROM reparacion_refaccion rr
                JOIN reparaciones r ON r.id_reparacion = rr.id_reparacion
                JOIN refacciones f ON f.id_refaccion = rr.id_refaccion
                WHERE r.folio = ?
                ORDER BY f.nombre
                """;
        try (Connection c = ConexionBD.obtener(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, folio);
            try (ResultSet r = p.executeQuery()) {
                List<Aplicada> lista = new ArrayList<>();
                while (r.next()) lista.add(new Aplicada(r.getString(1), r.getInt(2), r.getBigDecimal(3)));
                return lista;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudieron consultar las piezas de la reparación.", e);
        }
    }

    public void asignar(String folio, int idRefaccion, String unidades) {
        Sesion.exigirRol(Rol.ADMINISTRADOR, Rol.TECNICO);
        if (folio == null || folio.isBlank()) throw new ValidacionException("Selecciona una reparación.");
        int cantidad = validarCantidad(unidades, false);

        try (Connection c = ConexionBD.obtener()) {
            c.setAutoCommit(false);
            try {
                int reparacion = bloquearReparacion(c, folio);
                BigDecimal costo = descontarStock(c, idRefaccion, cantidad);
                // Si la pieza ya está en el folio, conserva el costo unitario registrado inicialmente.
                String sql = """
                        INSERT INTO reparacion_refaccion (id_reparacion, id_refaccion, cantidad, costo_unitario)
                        VALUES (?, ?, ?, ?)
                        ON DUPLICATE KEY UPDATE cantidad = cantidad + ?
                        """;
                try (PreparedStatement p = c.prepareStatement(sql)) {
                    p.setInt(1, reparacion);
                    p.setInt(2, idRefaccion);
                    p.setInt(3, cantidad);
                    p.setBigDecimal(4, costo);
                    p.setInt(5, cantidad);
                    p.executeUpdate();
                }
                c.commit();
            } catch (RuntimeException | SQLException e) {
                try { c.rollback(); } catch (SQLException rollback) { e.addSuppressed(rollback); }
                throw e;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo asignar la refacción. No se completó el movimiento.", e);
        }
    }

    private static int bloquearReparacion(Connection c, String folio) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "SELECT id_reparacion, estado FROM reparaciones WHERE folio = ? FOR UPDATE")) {
            p.setString(1, folio);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) throw new ReglaNegocioException("El folio ya no existe.");
                if ("ENTREGADO".equals(r.getString(2))) {
                    throw new ReglaNegocioException("No se pueden agregar refacciones a una reparación entregada.");
                }
                return r.getInt(1);
            }
        }
    }

    private static BigDecimal descontarStock(Connection c, int idRefaccion, int cantidad) throws SQLException {
        BigDecimal costo;
        try (PreparedStatement p = c.prepareStatement(
                "SELECT costo, stock FROM refacciones WHERE id_refaccion = ? FOR UPDATE")) {
            p.setInt(1, idRefaccion);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) throw new ReglaNegocioException("La refacción ya no existe.");
                int stock = r.getInt(2);
                if (stock < cantidad) {
                    throw new ReglaNegocioException("No hay existencias suficientes. Disponibles: " + stock + ".");
                }
                costo = r.getBigDecimal(1);
            }
        }
        try (PreparedStatement p = c.prepareStatement("UPDATE refacciones SET stock = stock - ? WHERE id_refaccion = ?")) {
            p.setInt(1, cantidad);
            p.setInt(2, idRefaccion);
            p.executeUpdate();
        }
        return costo;
    }

    static int validarCantidad(String texto, boolean permiteCero) {
        try {
            String dato = texto == null ? "" : texto.strip();
            if (!dato.matches("[0-9]+")) throw new NumberFormatException();
            int cantidad = Integer.parseInt(dato);
            if (permiteCero || cantidad > 0) return cantidad;
        } catch (NumberFormatException ignored) {}
        throw new ValidacionException(permiteCero
                ? "Escribe una cantidad entera de cero o más." : "Escribe una cantidad entera mayor que cero.");
    }

    static BigDecimal validarCosto(String texto) {
        try {
            String dato = texto == null ? "" : texto.strip();
            if (!dato.matches("[0-9]+(\\.[0-9]{1,2})?")) throw new NumberFormatException();
            BigDecimal costo = new BigDecimal(dato).setScale(2, RoundingMode.UNNECESSARY);
            if (costo.compareTo(new BigDecimal("99999999.99")) <= 0) return costo;
        } catch (NumberFormatException ignored) {}
        throw new ValidacionException("Escribe un costo válido, sin símbolo de pesos ni comas. Por ejemplo, 350.50.");
    }
}
