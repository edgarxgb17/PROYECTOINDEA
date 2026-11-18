package com.indea.util;

import com.indea.excepcion.PersistenciaException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Crea o actualiza las cuentas iniciales sin duplicarlas en cada arranque. */
public final class DatosIniciales {
    private DatosIniciales() { }

    public static void crearSiVacio() {
        try (Connection c = ConexionBD.obtener()) {
            try (Statement st=c.createStatement(); ResultSet rs=st.executeQuery("SELECT COUNT(*) FROM usuarios")) {rs.next(); if(rs.getInt(1)>0)return;}
            c.setAutoCommit(false);
            try {
                // Migra las cuentas de demostración de versiones anteriores y las reemplaza
                // por las cuentas nominales acordadas para el equipo INDEA.
                asegurarUsuario(c, "Ángel Leonardo Hernández Casarín", "2281000001", "ADMINISTRADOR", "admin", "admin123", "admin");
                asegurarUsuario(c, "Edgar Báez García", "2281000002", "TECNICO", "Edgar Báez García", "edgar123", "tecnico1");
                asegurarUsuario(c, "Óscar Daniel Hernández Hernández", "2281000005", "TECNICO", "Óscar Daniel Hernández Hernández", "oscar123", null);
                asegurarUsuario(c, "Alan López", "2281000003", "VENDEDOR", "Alan López", "alan123", "vendedor1");
                asegurarUsuario(c, "Juan Manuel Hernández Cova", "2281000004", "MARKETING", "Juan Manuel Hernández Cova", "juan123", "marketing1");
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudieron preparar las cuentas iniciales. Verifica que ejecutaste sql/01_crear_base_indea.sql y configuraste config/db.properties.", e);
        }
    }

    private static void asegurarUsuario(Connection c, String nombre, String telefono, String rol,
                                        String usuario, String clave, String usuarioAnterior) throws SQLException {
        Integer idEmpleado = buscarEmpleadoDeUsuario(c, usuario);
        boolean migrado = false;
        if (idEmpleado == null && usuarioAnterior != null) {
            idEmpleado = buscarEmpleadoDeUsuario(c, usuarioAnterior);
            if (idEmpleado != null) {
                try (PreparedStatement ps = c.prepareStatement("UPDATE usuarios SET nombre_usuario=? WHERE id_empleado=?")) {
                    ps.setString(1, usuario);
                    ps.setInt(2, idEmpleado);
                    ps.executeUpdate();
                    migrado = true;
                }
            }
        }

        if (idEmpleado == null) {
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO empleados (nombre, telefono, rol, activo, fecha_ingreso) VALUES (?, ?, ?, TRUE, CURDATE())",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, nombre);
                ps.setString(2, telefono);
                ps.setString(3, rol);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) throw new SQLException("No se generó el identificador del empleado " + nombre);
                    idEmpleado = rs.getInt(1);
                }
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO usuarios (id_empleado, nombre_usuario, contrasena_hash, intentos_fallidos, bloqueado) VALUES (?, ?, ?, 0, FALSE)")) {
                ps.setInt(1, idEmpleado);
                ps.setString(2, usuario);
                ps.setString(3, SeguridadUtil.hashear(clave));
                ps.executeUpdate();
            }
        } else {
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE empleados SET nombre=?, telefono=?, rol=?, activo=TRUE WHERE id_empleado=?")) {
                ps.setString(1, nombre);
                ps.setString(2, telefono);
                ps.setString(3, rol);
                ps.setInt(4, idEmpleado);
                ps.executeUpdate();
            }
            if (migrado) {
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE usuarios SET nombre_usuario=?, contrasena_hash=?, intentos_fallidos=0, bloqueado=FALSE WHERE id_empleado=?")) {
                    ps.setString(1, usuario);
                    ps.setString(2, SeguridadUtil.hashear(clave));
                    ps.setInt(3, idEmpleado);
                    ps.executeUpdate();
                }
            }
        }
    }

    private static Integer buscarEmpleadoDeUsuario(Connection c, String usuario) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT id_empleado FROM usuarios WHERE nombre_usuario=?")) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }
}
