package com.indea.servicio;

import com.indea.excepcion.PersistenciaException;
import com.indea.excepcion.ReglaNegocioException;
import com.indea.excepcion.ValidacionException;
import com.indea.modelo.Rol;
import com.indea.util.ConexionBD;
import com.indea.util.Sesion;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class ProveedorService {
    public record Ficha(String nombre, String contacto, String telefono, String correo, String direccion) {}
    public record Proveedor(int id, Ficha ficha) {}
    public record Compra(Timestamp fecha, String equipo, int cantidad, BigDecimal costoUnitario, BigDecimal total) {}

    static void permiso() { Sesion.exigirRol(Rol.ADMINISTRADOR, Rol.VENDEDOR); }

    public static Ficha preparar(String nombre, String contacto, String telefono, String correo, String direccion) {
        String n = limpiar(nombre,100,"nombre comercial");
        if (n == null) throw new ValidacionException("Escribe el nombre comercial.");
        String tel = limpiar(telefono,30,"teléfono");
        if (tel != null && !tel.matches("[+0-9 ()-]{7,30}")) {
            throw new ValidacionException("Revisa el número de teléfono.");
        }
        String mail = limpiar(correo,100,"correo");
        if (mail != null && !mail.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
            throw new ValidacionException("Escribe un correo válido.");
        }
        return new Ficha(n,limpiar(contacto,100,"contacto"),tel,mail,limpiar(direccion,200,"dirección"));
    }

    private static String limpiar(String texto, int max, String campo) {
        if (texto == null || texto.isBlank()) return null;
        String limpio = texto.strip().replaceAll("\\s+", " ");
        if (limpio.length() > max) throw new ValidacionException("El campo " + campo + " es demasiado largo.");
        return limpio;
    }

    public List<Proveedor> listar() {
        permiso();
        try (Connection c = ConexionBD.obtener();
             PreparedStatement p = c.prepareStatement("SELECT id_proveedor,nombre,contacto,telefono,correo,direccion FROM proveedores ORDER BY nombre");
             ResultSet r = p.executeQuery()) {
            List<Proveedor> lista = new ArrayList<>();
            while (r.next()) lista.add(new Proveedor(r.getInt(1),new Ficha(r.getString(2),r.getString(3),
                    r.getString(4),r.getString(5),r.getString(6))));
            return lista;
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo cargar el catálogo de proveedores. Verifica que la base incluya el catálogo.",e);
        }
    }

    public int registrar(Ficha ficha) {
        permiso();
        try (Connection c = ConexionBD.obtener()) { return insertar(c,ficha); }
        catch (SQLException e) { throw new PersistenciaException("No se pudo registrar el proveedor.",e); }
    }

    static int insertar(Connection c, Ficha ficha) throws SQLException {
        Ficha validada=preparar(ficha.nombre(),ficha.contacto(),ficha.telefono(),ficha.correo(),ficha.direccion());
        try (PreparedStatement p=c.prepareStatement(
                "INSERT INTO proveedores(nombre,contacto,telefono,correo,direccion) VALUES(?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)) {
            cargar(p,validada);
            p.executeUpdate();
            try (ResultSet r=p.getGeneratedKeys()) {
                if (!r.next()) throw new SQLException("No se generó el identificador del proveedor");
                return r.getInt(1);
            }
        } catch (SQLException e) {
            if (e.getErrorCode()==1062) throw new ReglaNegocioException("Ese proveedor ya está registrado. Selecciónalo en el catálogo.");
            throw e;
        }
    }

    public void actualizar(int id, Ficha ficha) {
        permiso();
        Ficha validada=preparar(ficha.nombre(),ficha.contacto(),ficha.telefono(),ficha.correo(),ficha.direccion());
        try (Connection c=ConexionBD.obtener(); PreparedStatement p=c.prepareStatement(
                "UPDATE proveedores SET nombre=?, contacto=?, telefono=?, correo=?, direccion=? WHERE id_proveedor=?")) {
            cargar(p,validada);
            p.setInt(6,id);
            p.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode()==1062) throw new ReglaNegocioException("Ya hay un proveedor con ese nombre.");
            throw new PersistenciaException("No se pudo actualizar el proveedor.",e);
        }
    }

    private static void cargar(PreparedStatement p,Ficha f) throws SQLException {
        p.setString(1,f.nombre()); p.setString(2,f.contacto()); p.setString(3,f.telefono());
        p.setString(4,f.correo()); p.setString(5,f.direccion());
    }

    static String nombreActual(Connection c,int id) throws SQLException {
        try (PreparedStatement p=c.prepareStatement("SELECT nombre FROM proveedores WHERE id_proveedor=?")) {
            p.setInt(1,id);
            try (ResultSet r=p.executeQuery()) {
                if (!r.next()) throw new ReglaNegocioException("El proveedor elegido ya no existe.");
                return r.getString(1);
            }
        }
    }

    public List<Compra> historial(int id) {
        permiso();
        String sql = """
                SELECT MIN(c.fecha_compra),
                       MIN(CONCAT_WS(' ',e.tipo,NULLIF(e.marca,''),NULLIF(e.modelo,''))),
                       COUNT(*), MIN(c.precio_compra), SUM(c.precio_compra)
                FROM compras c JOIN equipos e ON e.id_equipo=c.id_equipo
                WHERE c.id_proveedor=?
                GROUP BY COALESCE(c.lote_compra, CONCAT('compra-',c.id_compra))
                ORDER BY MIN(c.fecha_compra) DESC, MIN(c.id_compra) DESC
                """;
        try (Connection c=ConexionBD.obtener(); PreparedStatement p=c.prepareStatement(sql)) {
            p.setInt(1,id);
            try (ResultSet r=p.executeQuery()) {
                List<Compra> lista=new ArrayList<>();
                while (r.next()) lista.add(new Compra(r.getTimestamp(1),r.getString(2),r.getInt(3),r.getBigDecimal(4),r.getBigDecimal(5)));
                return lista;
            }
        } catch (SQLException e) { throw new PersistenciaException("No se pudo consultar el historial del proveedor.",e); }
    }
}
