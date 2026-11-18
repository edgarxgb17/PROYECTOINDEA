package com.indea.vista;
import com.indea.servicio.NegocioService;
import com.indea.servicio.NegocioService.PersonaOperacion;
import com.indea.servicio.ClienteService;
import com.indea.modelo.Cliente;
import com.indea.util.Sesion;
import com.indea.modelo.Rol;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Ventana de operaciones con tablas de solo lectura y formularios validados. */
public class NegocioFrame extends JPanel {
 private final NegocioService s=new NegocioService();
 private final JTable tabla=new JTable();
 private final JPanel botones=new JPanel();
 private final String vista;
 private String filtro="";
 private JTabbedPane pestanasInventario;
 private RefaccionesPanel refacciones;
 public NegocioFrame(String vista){
  this.vista=vista;
  setLayout(new BorderLayout());
  add(Ui.encabezado(vista.toUpperCase(java.util.Locale.ROOT),"Sistema de gestión INDEA"),BorderLayout.NORTH);
  if ("inventario".equals(vista) && (Sesion.getRol()==Rol.ADMINISTRADOR || Sesion.getRol()==Rol.TECNICO)) {
   JPanel equipos=new JPanel(new BorderLayout());
   equipos.add(new JScrollPane(tabla),BorderLayout.CENTER);
   equipos.add(botones,BorderLayout.SOUTH);
   pestanasInventario=new JTabbedPane();
   refacciones=new RefaccionesPanel();
   pestanasInventario.addTab("Equipos",equipos);
   pestanasInventario.addTab("Refacciones",refacciones);
   pestanasInventario.addChangeListener(e->{
    try { refresh(); } catch(RuntimeException ex) { Ui.manejar(this,ex); }
   });
   add(pestanasInventario,BorderLayout.CENTER);
  } else {
   add(new JScrollPane(tabla),BorderLayout.CENTER);
   add(botones,BorderLayout.SOUTH);
  }
 switch(vista){
 case "inventario":
  boton("Filtrar",()->filtro=choose("Estado",new String[]{"","DISPONIBLE","VENDIDO","NO_DISPONIBLE_AUN"}));
  if (Sesion.getRol()==Rol.ADMINISTRADOR || Sesion.getRol()==Rol.VENDEDOR) {
   boton("Compra",this::compra);
   boton("Venta",this::venta);
   boton("Disponibilidad",()->s.disponibilidad(id(),choose("Estado",new String[]{"DISPONIBLE","NO_DISPONIBLE_AUN"})));
  }
  break;
 case "empleados":boton("Nuevo",()->empleado(false));boton("Editar / desbloquear",()->empleado(true));break;
 case "publicaciones":boton("Nueva",()->pub(false));boton("Editar / desactivar",()->pub(true));break;
 case "historial":boton("Consultar cliente",()->filtro=String.valueOf(persona("Consultar historial",false).id()));break;
 case "reportes":boton("Día / semana / mes",()->{String p=choose("Periodo",new String[]{"Día","Semana","Mes"});LocalDate h=LocalDate.now();LocalDate d=p.equals("Día")?h:p.equals("Semana")?h.minusDays(h.getDayOfWeek().getValue()-1):h.withDayOfMonth(1);filtro=d+","+h;refresh();});break;
 }boton("Actualizar",()->refresh());refresh();}
 private void boton(String t,Runnable r){JButton b=new JButton(t);botones.add(b);b.addActionListener(e->{try{r.run();refresh();}catch(Cancel x){}catch(RuntimeException x){Ui.manejar(this,x);}});}
 private static class Cancel extends RuntimeException{}
 private String ask(String t){String a=JOptionPane.showInputDialog(this,t);if(a==null)throw new Cancel();return a;}
 private String choose(String t,String[] opts){Object a=JOptionPane.showInputDialog(this,t,t,JOptionPane.QUESTION_MESSAGE,null,opts,opts[0]);if(a==null)throw new Cancel();return a.toString();}
 private String[] form(String title,String... labels){JPanel p=new JPanel(new GridLayout(0,2,5,5));JTextField[] f=new JTextField[labels.length];for(int i=0;i<f.length;i++){p.add(new JLabel(labels[i]));f[i]=labels[i].contains("Contraseña")?new JPasswordField():new JTextField(22);p.add(f[i]);}if(JOptionPane.showConfirmDialog(this,p,title,JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)throw new Cancel();String[] a=new String[f.length];for(int i=0;i<a.length;i++)a[i]=f[i].getText();return a;}
 private int id(){if(tabla.getSelectedRow()<0)throw new IllegalArgumentException("Selecciona una fila");return Integer.parseInt(tabla.getValueAt(tabla.getSelectedRow(),0).toString());}
 private void empleado(boolean edit){int id=edit?id():0;String[] a=form("Empleado","Nombre","Teléfono","Correo","Usuario","Contraseña (vacía conserva)");s.empleado(id,a[0],a[1],a[2],choose("Rol",new String[]{"ADMINISTRADOR","TECNICO","VENDEDOR","MARKETING"}),a[3],a[4],choose("Activo",new String[]{"Sí","No"}).equals("Sí"));}
 private void compra() {
  String[] a=form("Compra","Tipo","Marca","Modelo","Condición","Precio venta","Precio compra");
  String origen=choose("¿A quién le compra INDEA?",new String[]{"Persona","Proveedor"});
  PersonaOperacion persona="Persona".equals(origen) ? persona("Persona que vende el equipo",true).datos() : null;
  String proveedor="Proveedor".equals(origen) ? ask("Nombre del proveedor") : null;
  String estado=choose("Estado",new String[]{"DISPONIBLE","NO_DISPONIBLE_AUN"});
  s.compra(a[0],a[1],a[2],a[3],a[4],a[5],persona!=null?"CLIENTE":"PROVEEDOR",persona,proveedor,estado);
 }

 private void venta() {
  int equipo=id();
  String[] a=form("Venta","Precio venta");
  PersonaOperacion comprador=persona("Persona que compra el equipo",true).datos();
  s.venta(equipo,comprador,a[0]);
 }

 private record PersonaElegida(int id, PersonaOperacion datos) {}

 private PersonaElegida persona(String titulo,boolean permitirAlta) {
  List<SelectorOperacion.Opcion<PersonaElegida>> opciones=new ArrayList<>();
  for (Cliente c : new ClienteService().buscarClientes("")) {
   String etiqueta=c.getNombre();
   if (c.getTelefono()!=null && !c.getTelefono().isBlank()) etiqueta+=" · "+c.getTelefono();
   if (c.getCorreo()!=null && !c.getCorreo().isBlank()) etiqueta+=" · "+c.getCorreo();
   opciones.add(new SelectorOperacion.Opcion<>(new PersonaElegida(c.getId(),PersonaOperacion.existente(c.getId())),
           etiqueta,c.getTelefono()));
  }
  PersonaElegida elegida=SelectorOperacion.abrir(this,titulo,"Buscar por nombre, teléfono o correo",opciones,
          "Registrar persona nueva",permitirAlta ? this::altaPersona : null);
  if (elegida==null) throw new Cancel();
  return elegida;
 }

 private PersonaElegida altaPersona() {
  JTextField nombre=new JTextField(24),telefono=new JTextField(24),correo=new JTextField(24);
  JPanel campos=new JPanel(new GridLayout(0,2,6,6));
  campos.add(new JLabel("Nombre")); campos.add(nombre);
  campos.add(new JLabel("Teléfono")); campos.add(telefono);
  campos.add(new JLabel("Correo")); campos.add(correo);
  JPanel contenido=new JPanel(new BorderLayout(0,8));
  contenido.add(new JLabel("Escribe un teléfono o correo. El alta se guarda al confirmar la operación."),BorderLayout.NORTH);
  contenido.add(campos,BorderLayout.CENTER);
  while (JOptionPane.showConfirmDialog(this,contenido,"Registrar persona nueva",
          JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE)==JOptionPane.OK_OPTION) {
   try {
    return new PersonaElegida(0,PersonaOperacion.nueva(nombre.getText(),telefono.getText(),correo.getText()));
   } catch (RuntimeException ex) { Ui.manejar(this,ex); }
  }
  return null;
 }

 private int seleccionarEquipo() {
  List<SelectorOperacion.Opcion<Integer>> opciones=new ArrayList<>();
  for (Object[] fila : s.query("inventario","","")) {
   int equipo=((Number)fila[0]).intValue();
   String etiqueta=(Objects.toString(fila[1],"")+" "+Objects.toString(fila[2],"")+" "
           +Objects.toString(fila[3],"")).strip()+" · "+Objects.toString(fila[6],"").replace('_',' ')
           +" · #"+equipo;
   opciones.add(new SelectorOperacion.Opcion<>(equipo,etiqueta,null));
  }
  if (opciones.isEmpty()) throw new IllegalArgumentException("Todavía no hay equipos en Inventario.");
  Integer elegido=SelectorOperacion.abrir(this,"Seleccionar equipo","Buscar por tipo, marca o modelo",opciones,null,null);
  if (elegido==null) throw new Cancel();
  return elegido;
 }

 private void pub(boolean edit) {
  int id=edit?id():0;
  int fila=tabla.getSelectedRow();
  int equipo=edit?Integer.parseInt(tabla.getValueAt(fila,1).toString()):seleccionarEquipo();
  JTextField titulo=new JTextField(edit?Objects.toString(tabla.getValueAt(fila,2),""):"",25);
  JTextField descripcion=new JTextField(edit?Objects.toString(tabla.getValueAt(fila,3),""):"",25);
  JComboBox<String> tipo=new JComboBox<>(new String[]{"DISPONIBLE","AGOTADO","PROXIMAMENTE"});
  Object estadoActual=edit?tabla.getValueAt(fila,5):Boolean.TRUE;
  boolean activaActual=estadoActual instanceof Number n?n.intValue()!=0:Boolean.parseBoolean(Objects.toString(estadoActual));
  JCheckBox activa=new JCheckBox("Activa",activaActual);
  if (edit) tipo.setSelectedItem(Objects.toString(tabla.getValueAt(fila,4)));
  JPanel p=new JPanel(new GridLayout(0,2,5,5));
  p.add(new JLabel("Título")); p.add(titulo);
  p.add(new JLabel("Descripción")); p.add(descripcion);
  p.add(new JLabel("Tipo")); p.add(tipo);
  p.add(new JLabel("Publicación")); p.add(activa);
  if (JOptionPane.showConfirmDialog(this,p,"Publicación",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION) throw new Cancel();
  s.publicacion(id,equipo,titulo.getText(),descripcion.getText(),(String)tipo.getSelectedItem(),activa.isSelected());
 }
 void refresh(){
  if (pestanasInventario!=null && pestanasInventario.getSelectedComponent()==refacciones) {
   refacciones.refrescar();
   return;
  }
  Object[][] data;String[] cols;switch(vista){case "inventario":data=s.query(vista,filtro,filtro);cols=new String[]{"ID","Tipo","Marca","Modelo","Condición","Precio","Estado"};break;case "empleados":data=s.query(vista);cols=new String[]{"ID","Nombre","Teléfono","Correo","Rol","Activo","Usuario"};break;case "publicaciones":data=s.query(vista);cols=new String[]{"ID","Equipo","Título","Descripción","Tipo","Activa"};break;case "historial":data=filtro.isEmpty()?new Object[0][]:s.query(vista,Integer.parseInt(filtro),Integer.parseInt(filtro),Integer.parseInt(filtro));cols=new String[]{"Operación","Referencia","Fecha","Importe","Estado"};break;default:if(filtro.isEmpty())filtro=LocalDate.now()+","+LocalDate.now();String[] d=filtro.split(",");data=s.query(vista,d[0],d[1],d[0],d[1]);cols=new String[]{"Concepto","Ingresos"};}tabla.setModel(new DefaultTableModel(data,cols){public boolean isCellEditable(int r,int c){return false;}});}
}
