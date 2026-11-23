package com.indea.vista;
import com.indea.servicio.NegocioService;
import com.indea.servicio.NegocioService.PersonaOperacion;
import com.indea.servicio.NegocioService.ProveedorCompra;
import com.indea.servicio.ProveedorService;
import com.indea.servicio.ReportesPdfService;
import com.indea.servicio.ClienteService;
import com.indea.modelo.Cliente;
import com.indea.util.Sesion;
import com.indea.modelo.Rol;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Ventana de operaciones con tablas de solo lectura y formularios validados. */
public class NegocioFrame extends JPanel {
 private final NegocioService s=new NegocioService();
 private final JTable tabla=new TablaIndea();
 private final JPanel botones=new JPanel();
 private final JLabel detalle=new JLabel();
 private static final DateTimeFormatter DIA=DateTimeFormatter.ofPattern("dd/MM/yyyy");
 private final String vista;
 private String filtro="";
 private JTabbedPane pestanasInventario;
 private RefaccionesPanel refacciones;
 private ProveedoresPanel proveedores;
 public NegocioFrame(String vista){
  this.vista=vista;
  setLayout(new BorderLayout(0, 10));
  setBorder(BorderFactory.createEmptyBorder(4, 14, 10, 14));
  add(Ui.encabezado(vista.substring(0,1).toUpperCase(java.util.Locale.ROOT) + vista.substring(1),"Sistema de gestión INDEA"),BorderLayout.NORTH);
  Rol rol=Sesion.getRol();
  if ("inventario".equals(vista) && (rol==Rol.ADMINISTRADOR || rol==Rol.TECNICO || rol==Rol.VENDEDOR)) {
   JPanel equipos=new JPanel(new BorderLayout());
   equipos.add(new JScrollPane(tabla),BorderLayout.CENTER);
   equipos.add(botones,BorderLayout.SOUTH);
   pestanasInventario=new JTabbedPane();
   pestanasInventario.addTab("Equipos",equipos);
   if (rol==Rol.ADMINISTRADOR || rol==Rol.TECNICO) {
    refacciones=new RefaccionesPanel();
    pestanasInventario.addTab("Refacciones",refacciones);
   }
   if (rol==Rol.ADMINISTRADOR || rol==Rol.VENDEDOR) {
    proveedores=new ProveedoresPanel();
    pestanasInventario.addTab("Proveedores",proveedores);
   }
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
  boton("Filtrar",()->{
   String elegido=choose("Estado",new String[]{"Todos","Disponibles","No disponibles aún","Vendidos"});
   filtro=switch(elegido){case "Disponibles"->"DISPONIBLE";case "No disponibles aún"->"NO_DISPONIBLE_AUN";case "Vendidos"->"VENDIDO";default->"";};
  });
  boton("PDF de inventario",()->ExportarPdf.guardar(this,"INDEA-inventario",()->ReportesPdfService.inventario(filtro)));
  if (Sesion.getRol()==Rol.ADMINISTRADOR || Sesion.getRol()==Rol.VENDEDOR) {
   boton("Compra",this::compra);
   boton("Venta",this::venta);
   boton("Disponibilidad",()->s.disponibilidad(id(),choose("Estado",new String[]{"DISPONIBLE","NO_DISPONIBLE_AUN"})));
  }
  break;
 case "empleados":boton("Nuevo",()->empleado(false));boton("Editar / desbloquear",()->empleado(true));break;
 case "publicaciones":boton("Nueva",()->pub(false));boton("Editar / desactivar",()->pub(true));break;
 case "historial":
  boton("Consultar cliente",()->filtro=String.valueOf(persona("Consultar historial",false).id()));
  boton("Ver todos",()->filtro="");
  botones.add(detalle);
  break;
 case "reportes":
  boton("Periodo",this::elegirPeriodo);
  boton("PDF de ganancias",()->{
   LocalDate[] fechas=periodoReporte();
   ExportarPdf.guardar(this,"INDEA-ganancias-"+fechas[0]+"-a-"+fechas[1],
           ()->ReportesPdfService.ganancias(fechas[0],fechas[1]));
  });
  botones.add(detalle);
  break;
 }boton("Actualizar",()->refresh());refresh();}
 private void boton(String t,Runnable r){JButton b=new BotonIndea(t, java.util.Set.of("Compra","Nuevo","Nueva","Consultar cliente","PDF de ganancias").contains(t));botones.add(b);b.addActionListener(e->{try{r.run();refresh();}catch(Cancel x){}catch(RuntimeException x){Ui.manejar(this,x);}});}
 private static class Cancel extends RuntimeException{}
 private String ask(String t){String a=JOptionPane.showInputDialog(this,t);if(a==null)throw new Cancel();return a;}
 private String choose(String t,String[] opts){Object a=JOptionPane.showInputDialog(this,t,t,JOptionPane.QUESTION_MESSAGE,null,opts,opts[0]);if(a==null)throw new Cancel();return a.toString();}
 private String[] form(String title,String... labels){JPanel p=new JPanel(new GridLayout(0,2,5,5));JTextField[] f=new JTextField[labels.length];for(int i=0;i<f.length;i++){p.add(new JLabel(labels[i]));f[i]=labels[i].contains("Contraseña")?new JPasswordField():new JTextField(22);if(labels[i].equals("Cantidad de equipos"))f[i].setText("1");p.add(f[i]);}if(JOptionPane.showConfirmDialog(this,p,title,JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)throw new Cancel();String[] a=new String[f.length];for(int i=0;i<a.length;i++)a[i]=f[i].getText();return a;}
 private int id(){if(tabla.getSelectedRow()<0)throw new IllegalArgumentException("Selecciona una fila");return Integer.parseInt(tabla.getValueAt(tabla.getSelectedRow(),0).toString());}
 private void empleado(boolean edit){int id=edit?id():0;String[] a=form("Empleado","Nombre","Teléfono","Correo","Usuario","Contraseña (vacía conserva)");s.empleado(id,a[0],a[1],a[2],choose("Rol",new String[]{"ADMINISTRADOR","TECNICO","VENDEDOR","MARKETING"}),a[3],a[4],choose("Activo",new String[]{"Sí","No"}).equals("Sí"));}
 private LocalDate[] periodoReporte() {
  LocalDate hoy=LocalDate.now();
  if(filtro.isBlank())return new LocalDate[]{hoy.withDayOfMonth(1),hoy};
  String[] fechas=filtro.split(",");
  return new LocalDate[]{LocalDate.parse(fechas[0]),LocalDate.parse(fechas[1])};
 }

 private void elegirPeriodo() {
  String elegido=choose("Periodo",new String[]{"Hoy","Semana actual","Mes actual","Todo el historial","Elegir fechas"});
  LocalDate fin=LocalDate.now(),inicio=fin;
  if ("Semana actual".equals(elegido)) inicio=fin.minusDays(fin.getDayOfWeek().getValue()-1);
  else if ("Mes actual".equals(elegido)) inicio=fin.withDayOfMonth(1);
  else if ("Todo el historial".equals(elegido)) inicio=LocalDate.of(1900,1,1);
  else if ("Elegir fechas".equals(elegido)) {
   LocalDate[] anterior=periodoReporte();
   JTextField desde=new JTextField(anterior[0].toString(),12),hasta=new JTextField(anterior[1].toString(),12);
   JPanel campos=Ui.formulario();
   Ui.fila(campos,0,"Desde (AAAA-MM-DD)",desde);
   Ui.fila(campos,1,"Hasta (AAAA-MM-DD)",hasta);
   if (JOptionPane.showConfirmDialog(this,campos,"Elegir periodo",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION) throw new Cancel();
   try { inicio=LocalDate.parse(desde.getText().strip()); fin=LocalDate.parse(hasta.getText().strip()); }
   catch (java.time.format.DateTimeParseException e) { throw new IllegalArgumentException("Escribe fechas como AAAA-MM-DD."); }
  }
  if (fin.isBefore(inicio)||inicio.getYear()<1000||fin.getYear()>9998)
   throw new IllegalArgumentException("Revisa que el periodo sea válido y que la fecha final no sea anterior a la inicial.");
  filtro=inicio+","+fin;
 }

 private void compra() {
  String[] a=form("Compra de equipos","Tipo","Marca","Modelo","Condición","Precio de venta por equipo","Costo de compra por equipo","Cantidad de equipos");
  int cantidad=NegocioService.cantidadEquipos(a[6]);
  String origen=choose("¿A quién le compra INDEA?",new String[]{"Persona","Proveedor"});
  PersonaOperacion persona="Persona".equals(origen)?persona("Persona que vende el equipo",true).datos():null;
  ProveedorCompra proveedor="Proveedor".equals(origen)?seleccionarProveedor():null;
  String estado=choose("Estado inicial de los equipos",new String[]{"DISPONIBLE","NO_DISPONIBLE_AUN"});
  if (!Ui.confirmar(this,"¿Registrar la compra de "+cantidad+" equipo(s)? Se agregarán por separado al inventario.")) return;
  s.compra(a[0],a[1],a[2],a[3],a[4],a[5],persona,proveedor,estado,cantidad);
  Ui.info(this,"Compra registrada. Se agregaron "+cantidad+" equipo(s) al inventario.");
 }

 private ProveedorCompra seleccionarProveedor() {
  List<SelectorOperacion.Opcion<ProveedorCompra>> opciones=new ArrayList<>();
  for (var p:new ProveedorService().listar()) {
   var f=p.ficha();
   String etiqueta=f.nombre()+(f.contacto()==null?"":" · "+f.contacto())+(f.telefono()==null?"":" · "+f.telefono());
   opciones.add(new SelectorOperacion.Opcion<>(new ProveedorCompra(p.id(),null),etiqueta,f.telefono()));
  }
  ProveedorCompra elegido=SelectorOperacion.abrir(this,"Proveedor de la compra","Busca por nombre, contacto o teléfono",
          opciones,"Registrar proveedor nuevo",()->{
   var ficha=ProveedoresPanel.formulario(this,null,true);
   return ficha==null?null:new ProveedorCompra(null,ficha);
  });
  if (elegido==null) throw new Cancel();
  return elegido;
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
  if (pestanasInventario!=null && pestanasInventario.getSelectedComponent()==proveedores) {
   proveedores.refrescar();
   return;
  }
  Object[][] data;
  String[] cols;
  switch(vista){
   case "inventario":
    data=s.query(vista,filtro,filtro);
    cols=new String[]{"ID","Tipo","Marca","Modelo","Condición","Precio","Estado"};
    break;
   case "empleados":
    data=s.query(vista);
    cols=new String[]{"ID","Nombre","Teléfono","Correo","Rol","Activo","Usuario"};
    break;
   case "publicaciones":
    data=s.query(vista);
    cols=new String[]{"ID","Equipo","Título","Descripción","Tipo","Activa"};
    break;
   case "historial":
    int cliente=filtro.isEmpty()?0:Integer.parseInt(filtro);
    data=s.query(vista,cliente,cliente,cliente,cliente,cliente,cliente);
    cols=new String[]{"Operación","Referencia","Cliente / Origen","Fecha","Importe","Estado"};
    detalle.setText(cliente==0
            ?"  Todas las operaciones ("+data.length+")"
            :"  Operaciones del cliente seleccionado ("+data.length+")");
    break;
   case "reportes":
    LocalDate[] periodo=periodoReporte();
    data=s.query(vista,periodo[0],periodo[1],periodo[0],periodo[1]);
    BigDecimal total=BigDecimal.ZERO;
    for(Object[] fila:data) {
     if(fila[1]!=null) total=total.add(new BigDecimal(fila[1].toString()));
    }
    Object[][] conTotal=java.util.Arrays.copyOf(data,data.length+1);
    conTotal[data.length]=new Object[]{"Total de ingresos",total};
    data=conTotal;
    cols=new String[]{"Concepto","Ingresos (MXN)"};
    detalle.setText("  Periodo: "+periodo[0].format(DIA)+" al "+periodo[1].format(DIA));
    break;
   default:
    throw new IllegalArgumentException("Vista desconocida: "+vista);
  }
  tabla.setModel(new DefaultTableModel(data,cols){
    @Override public boolean isCellEditable(int r,int c){return false;}
  });
  if ("inventario".equals(vista)) {
   int[] anchos = {48, 100, 100, 165, 235, 85, 155};
   for (int i=0; i<anchos.length; i++) tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
  } else if ("historial".equals(vista)) {
   int[] anchos={105,130,205,156,115,137};
   for (int i=0;i<anchos.length;i++) tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
  }
 }

}
