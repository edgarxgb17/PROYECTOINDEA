package com.indea.servicio;
import com.indea.util.*;
import com.indea.modelo.Rol;
import com.indea.modelo.Cliente;
import java.sql.*;
import java.util.*;
import java.math.BigDecimal;

/** Operaciones de negocio adicionales; cada escritura es atómica. */
public class NegocioService {
 private void permiso(Rol... roles){Sesion.exigirRol(roles);}
 private int actor(){return Sesion.getEmpleado().getId();}
 private static void bind(PreparedStatement p,Object... a)throws SQLException{for(int i=0;i<a.length;i++)p.setObject(i+1,a[i]);}
 private static int exec(Connection c,String sql,Object... a)throws SQLException{try(PreparedStatement p=c.prepareStatement(sql)){bind(p,a);return p.executeUpdate();}}
 private static int insert(Connection c,String sql,Object... a)throws SQLException{try(PreparedStatement p=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){bind(p,a);p.executeUpdate();try(ResultSet r=p.getGeneratedKeys()){r.next();return r.getInt(1);}}}
 interface Work {void run(Connection c)throws SQLException;}
 private void tx(Work w){try(Connection c=ConexionBD.obtener()){c.setAutoCommit(false);try{w.run(c);c.commit();}catch(Exception e){c.rollback();throw e;}}catch(Exception e){throw new IllegalArgumentException(e.getMessage(),e);}}
 private static String required(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("Campo obligatorio");return s.trim();}
 private static BigDecimal money(String s){BigDecimal n=new BigDecimal(s);if(n.signum()<0||n.scale()>2||n.compareTo(new BigDecimal("99999999.99"))>0)throw new IllegalArgumentException("Importe inválido");return n;}
 public Object[][] query(String view,Object... args){String sql;switch(view){
 case "inventario": permiso(Rol.values());sql="SELECT id_equipo,tipo,marca,modelo,condicion,precio_venta,estado FROM equipos WHERE (?='' OR estado=?) ORDER BY id_equipo DESC";break;
 case "empleados": permiso(Rol.ADMINISTRADOR);sql="SELECT e.id_empleado,e.nombre,e.telefono,e.correo,e.rol,e.activo,u.nombre_usuario FROM empleados e LEFT JOIN usuarios u USING(id_empleado)";break;
 case "publicaciones": permiso(Rol.ADMINISTRADOR,Rol.MARKETING,Rol.VENDEDOR);sql="SELECT id_publicacion,id_equipo,titulo,descripcion,tipo,activa FROM publicaciones";break;
 case "historial": permiso(Rol.ADMINISTRADOR,Rol.TECNICO,Rol.VENDEDOR);sql="SELECT 'Reparación',folio,fecha_ingreso,costo_servicio,estado FROM reparaciones WHERE id_cliente=? UNION ALL SELECT 'Compra',id_compra,fecha_compra,precio_compra,origen FROM compras WHERE id_cliente=? UNION ALL SELECT 'Venta',id_venta,fecha_venta,precio_venta,'VENDIDO' FROM ventas WHERE id_cliente=? ORDER BY 3 DESC";break;
 case "reportes": permiso(Rol.ADMINISTRADOR);sql="SELECT 'Reparaciones',COALESCE(SUM(costo_servicio),0) FROM reparaciones WHERE pagado=1 AND fecha_pago>=? AND fecha_pago<DATE_ADD(?,INTERVAL 1 DAY) UNION ALL SELECT 'Ventas',COALESCE(SUM(precio_venta),0) FROM ventas WHERE fecha_venta>=? AND fecha_venta<DATE_ADD(?,INTERVAL 1 DAY)";break;
 case "panel": permiso(Rol.ADMINISTRADOR);sql="SELECT (SELECT COUNT(*) FROM reparaciones WHERE estado<>'ENTREGADO'),(SELECT COUNT(*) FROM equipos WHERE estado='DISPONIBLE'),(SELECT COUNT(*) FROM ventas WHERE fecha_venta>=DATE_FORMAT(CURDATE(),'%Y-%m-01')),(SELECT COALESCE(SUM(precio_venta),0) FROM ventas)+(SELECT COALESCE(SUM(costo_servicio),0) FROM reparaciones WHERE pagado=1)";break;
 default:throw new IllegalArgumentException("Consulta desconocida");}
 try(Connection c=ConexionBD.obtener();PreparedStatement p=c.prepareStatement(sql)){bind(p,args);try(ResultSet r=p.executeQuery()){List<Object[]> rows=new ArrayList<>();int n=r.getMetaData().getColumnCount();while(r.next()){Object[] row=new Object[n];for(int i=0;i<n;i++)row[i]=r.getObject(i+1);rows.add(row);}return rows.toArray(new Object[0][]);}}catch(SQLException e){throw new IllegalArgumentException(e.getMessage(),e);}}
 /** Persona elegida del catálogo o con datos de alta en la operación. */
 public static final class PersonaOperacion {
  private final int id;
  private final Cliente datos;
  private PersonaOperacion(int id, Cliente datos) { this.id = id; this.datos = datos; }
  public static PersonaOperacion existente(int id) {
   if (id <= 0) throw new IllegalArgumentException("Selecciona una persona registrada.");
   return new PersonaOperacion(id, null);
  }
  public static PersonaOperacion nueva(String nombre, String telefono, String correo) {
   return new PersonaOperacion(0, ClienteService.construir(0, nombre, telefono, correo, null));
  }
 }

 private int resolverPersona(Connection c, PersonaOperacion persona) throws SQLException {
  if (persona == null) throw new IllegalArgumentException("Selecciona una persona.");
  if (persona.datos == null) return persona.id;
  Cliente datos = persona.datos;
  String telefono = datos.getTelefono(), correo = datos.getCorreo();
  try (PreparedStatement p = c.prepareStatement(
          "SELECT nombre FROM clientes WHERE telefono = ? OR LOWER(correo) = LOWER(?) LIMIT 1")) {
   bind(p, telefono, correo);
   try (ResultSet r = p.executeQuery()) {
    if (r.next()) throw new IllegalArgumentException(
            "Ya existe un registro con ese teléfono o correo (" + r.getString(1) + "). Selecciónalo en la lista.");
   }
  }
  try {
   return insert(c, "INSERT INTO clientes(nombre,telefono,correo) VALUES(?,?,?)",
           datos.getNombre(), telefono, correo);
  } catch (SQLException e) {
   if (telefono == null && e.getErrorCode() == 1048) {
    throw new IllegalArgumentException("Esta base todavía requiere teléfono. Indícalo o aplica la actualización 02 de MySQL.", e);
   }
   if (e.getErrorCode() == 1062) {
    throw new IllegalArgumentException("Ese teléfono ya está registrado. Selecciona a la persona de la lista.", e);
   }
   throw e;
  }
 }

 public record ProveedorCompra(Integer id, ProveedorService.Ficha nueva) {
  public ProveedorCompra {
   if ((id==null)==(nueva==null) || (id!=null && id<=0)) {
    throw new IllegalArgumentException("Selecciona o registra un proveedor.");
   }
  }
 }

 public static int cantidadEquipos(String valor) {
  try {
   int n=Integer.parseInt(valor==null?"":valor.strip());
   if (n>0) return n;
  } catch (NumberFormatException ignored) {}
  throw new IllegalArgumentException("La cantidad de equipos debe ser un número entero mayor que cero.");
 }

 public void compra(String tipo,String marca,String modelo,String condicion,String precio,String costo,
                    PersonaOperacion persona,ProveedorCompra proveedor,String estado,int cantidad) {
  permiso(Rol.ADMINISTRADOR,Rol.VENDEDOR);
  if ((persona==null)==(proveedor==null)) throw new IllegalArgumentException("Selecciona el origen de la compra.");
  if (cantidad<=0) throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
  String tipoEquipo=required(tipo), condicionEquipo=required(condicion);
  BigDecimal precioVenta=money(precio), precioCompra=money(costo);
  String lote=UUID.randomUUID().toString();
  tx(c->{
   Integer cliente=persona!=null?resolverPersona(c,persona):null;
   Integer proveedorId=null;
   String proveedorNombre=null;
   if (proveedor!=null) {
    proveedorId=proveedor.id()!=null?proveedor.id():ProveedorService.insertar(c,proveedor.nueva());
    proveedorNombre=ProveedorService.nombreActual(c,proveedorId);
   }
   try (PreparedStatement altaEquipo=c.prepareStatement(
       "INSERT INTO equipos(tipo,marca,modelo,condicion,precio_venta,estado) VALUES(?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS);
        PreparedStatement altaCompra=c.prepareStatement(
       "INSERT INTO compras(id_equipo,origen,id_cliente,proveedor,id_empleado,precio_compra,id_proveedor,lote_compra) VALUES(?,?,?,?,?,?,?,?)")) {
    bind(altaEquipo,tipoEquipo,marca,modelo,condicionEquipo,precioVenta,estado);
    for (int i=0;i<cantidad;i++) {
     altaEquipo.executeUpdate();
     try (ResultSet ids=altaEquipo.getGeneratedKeys()) {
      if (!ids.next()) throw new SQLException("No se generó el identificador del equipo.");
      bind(altaCompra,ids.getInt(1),persona!=null?"CLIENTE":"PROVEEDOR",cliente,proveedorNombre,actor(),precioCompra,proveedorId,lote);
     }
     altaCompra.executeUpdate();
    }
   }
  });
 }

 public void venta(int equipo, PersonaOperacion comprador, String precio) {
  permiso(Rol.ADMINISTRADOR, Rol.VENDEDOR);
  BigDecimal precioVenta = money(precio);
  tx(c -> {
   if (exec(c, "UPDATE equipos SET estado='VENDIDO' WHERE id_equipo=? AND estado='DISPONIBLE' AND NOT EXISTS(SELECT 1 FROM publicaciones WHERE id_equipo=? AND activa=1 AND tipo<>'DISPONIBLE')", equipo, equipo) != 1) {
    throw new IllegalArgumentException("Equipo no disponible para venta");
   }
   int cliente = resolverPersona(c, comprador);
   exec(c, "INSERT INTO ventas(id_equipo,id_cliente,id_vendedor,precio_venta) VALUES(?,?,?,?)",
           equipo, cliente, actor(), precioVenta);
   exec(c, "UPDATE publicaciones SET tipo='AGOTADO' WHERE id_equipo=?", equipo);
  });
 }
 public void disponibilidad(int id,String estado){permiso(Rol.ADMINISTRADOR,Rol.VENDEDOR);tx(c->{if(exec(c,"UPDATE equipos SET estado=? WHERE id_equipo=? AND estado<>'VENDIDO'",estado,id)!=1)throw new IllegalArgumentException("Equipo inexistente o vendido");});}
 public void publicacion(int id,int equipo,String titulo,String descripcion,String tipo,boolean activa){permiso(Rol.ADMINISTRADOR,Rol.MARKETING,Rol.VENDEDOR);tx(c->{try(PreparedStatement p=c.prepareStatement("SELECT estado FROM equipos WHERE id_equipo=? FOR UPDATE")){p.setInt(1,equipo);try(ResultSet r=p.executeQuery()){if(!r.next())throw new IllegalArgumentException("Equipo inexistente");if(tipo.equals("DISPONIBLE")&&!r.getString(1).equals("DISPONIBLE"))throw new IllegalArgumentException("Equipo no disponible");}}if(id==0)exec(c,"INSERT INTO publicaciones(id_equipo,id_autor,titulo,descripcion,tipo,activa) VALUES(?,?,?,?,?,?)",equipo,actor(),required(titulo),descripcion,tipo,activa);else if(exec(c,"UPDATE publicaciones SET titulo=?,descripcion=?,tipo=?,activa=? WHERE id_publicacion=? AND id_equipo=?",required(titulo),descripcion,tipo,activa,id,equipo)!=1)throw new IllegalArgumentException("Publicación inexistente");});}
 public void empleado(int id,String nombre,String telefono,String correo,String rol,String usuario,String clave,boolean activo){permiso(Rol.ADMINISTRADOR);tx(c->{if(id==actor()&&(!activo||!rol.equals("ADMINISTRADOR")))throw new IllegalArgumentException("No puedes desactivar tu propia cuenta");if(id==0){int nuevo=insert(c,"INSERT INTO empleados(nombre,telefono,correo,rol,activo,fecha_ingreso) VALUES(?,?,?,?,?,CURDATE())",required(nombre),required(telefono),correo,rol,activo);exec(c,"INSERT INTO usuarios(id_empleado,nombre_usuario,contrasena_hash) VALUES(?,?,?)",nuevo,required(usuario),SeguridadUtil.hashear(required(clave)));}else{exec(c,"UPDATE empleados SET nombre=?,telefono=?,correo=?,rol=?,activo=? WHERE id_empleado=?",required(nombre),required(telefono),correo,rol,activo,id);exec(c,"UPDATE usuarios SET nombre_usuario=? WHERE id_empleado=?",required(usuario),id);if(!clave.isBlank())exec(c,"UPDATE usuarios SET contrasena_hash=?,bloqueado=0,intentos_fallidos=0 WHERE id_empleado=?",SeguridadUtil.hashear(clave),id);}});}
}
