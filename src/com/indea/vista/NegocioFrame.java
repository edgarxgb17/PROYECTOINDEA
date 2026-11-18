package com.indea.vista;
import com.indea.servicio.NegocioService;
import com.indea.util.Sesion;
import com.indea.modelo.Rol;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;

/** Ventana de operaciones con tablas de solo lectura y formularios validados. */
public class NegocioFrame extends JPanel {
 private final NegocioService s=new NegocioService();
 private final JTable tabla=new JTable();
 private final JPanel botones=new JPanel();
 private final String vista;
 private String filtro="";
 public NegocioFrame(String vista){this.vista=vista;setLayout(new BorderLayout());add(Ui.encabezado(vista.toUpperCase(),"Sistema de gestión INDEA"),BorderLayout.NORTH);add(new JScrollPane(tabla));add(botones,BorderLayout.SOUTH);
 switch(vista){
 case "inventario": boton("Filtrar",()->{filtro=choose("Estado",new String[]{"","DISPONIBLE","VENDIDO","NO_DISPONIBLE_AUN"});refresh();});if(Sesion.getRol()==Rol.ADMINISTRADOR||Sesion.getRol()==Rol.VENDEDOR){boton("Compra",()->{String[] a=form("Compra","Tipo","Marca","Modelo","Condición","Precio venta","Precio compra");String o=choose("Origen",new String[]{"CLIENTE","PROVEEDOR"});String f=ask(o.equals("CLIENTE")?"ID cliente":"Proveedor");s.compra(a[0],a[1],a[2],a[3],a[4],a[5],o,f,choose("Estado",new String[]{"DISPONIBLE","NO_DISPONIBLE_AUN"}));});boton("Venta",()->{String[] a=form("Venta","ID cliente","Precio venta");s.venta(id(),Integer.parseInt(a[0]),a[1]);});boton("Disponibilidad",()->s.disponibilidad(id(),choose("Estado",new String[]{"DISPONIBLE","NO_DISPONIBLE_AUN"})));}break;
 case "empleados":boton("Nuevo",()->empleado(false));boton("Editar / desbloquear",()->empleado(true));break;
 case "publicaciones":boton("Nueva",()->pub(false));boton("Editar / desactivar",()->pub(true));break;
 case "historial":boton("Consultar cliente",()->{filtro=ask("ID cliente (ver módulo Clientes)");refresh();});break;
 case "reportes":boton("Día / semana / mes",()->{String p=choose("Periodo",new String[]{"Día","Semana","Mes"});LocalDate h=LocalDate.now();LocalDate d=p.equals("Día")?h:p.equals("Semana")?h.minusDays(h.getDayOfWeek().getValue()-1):h.withDayOfMonth(1);filtro=d+","+h;refresh();});break;
 }boton("Actualizar",()->refresh());refresh();}
 private void boton(String t,Runnable r){JButton b=new JButton(t);botones.add(b);b.addActionListener(e->{try{r.run();refresh();}catch(Cancel x){}catch(RuntimeException x){Ui.manejar(this,x);}});}
 private static class Cancel extends RuntimeException{}
 private String ask(String t){String a=JOptionPane.showInputDialog(this,t);if(a==null)throw new Cancel();return a;}
 private String choose(String t,String[] opts){Object a=JOptionPane.showInputDialog(this,t,t,JOptionPane.QUESTION_MESSAGE,null,opts,opts[0]);if(a==null)throw new Cancel();return a.toString();}
 private String[] form(String title,String... labels){JPanel p=new JPanel(new GridLayout(0,2,5,5));JTextField[] f=new JTextField[labels.length];for(int i=0;i<f.length;i++){p.add(new JLabel(labels[i]));f[i]=labels[i].contains("Contraseña")?new JPasswordField():new JTextField(22);p.add(f[i]);}if(JOptionPane.showConfirmDialog(this,p,title,JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)throw new Cancel();String[] a=new String[f.length];for(int i=0;i<a.length;i++)a[i]=f[i].getText();return a;}
 private int id(){if(tabla.getSelectedRow()<0)throw new IllegalArgumentException("Selecciona una fila");return Integer.parseInt(tabla.getValueAt(tabla.getSelectedRow(),0).toString());}
 private void empleado(boolean edit){int id=edit?id():0;String[] a=form("Empleado","Nombre","Teléfono","Correo","Usuario","Contraseña (vacía conserva)");s.empleado(id,a[0],a[1],a[2],choose("Rol",new String[]{"ADMINISTRADOR","TECNICO","VENDEDOR","MARKETING"}),a[3],a[4],choose("Activo",new String[]{"Sí","No"}).equals("Sí"));}
 private void pub(boolean edit){int id=edit?id():0;String[] a=form("Publicación","ID equipo","Título","Descripción");s.publicacion(id,Integer.parseInt(a[0]),a[1],a[2],choose("Tipo",new String[]{"DISPONIBLE","AGOTADO","PROXIMAMENTE"}),choose("Activa",new String[]{"Sí","No"}).equals("Sí"));}
 void refresh(){Object[][] data;String[] cols;switch(vista){case "inventario":data=s.query(vista,filtro,filtro);cols=new String[]{"ID","Tipo","Marca","Modelo","Condición","Precio","Estado"};break;case "empleados":data=s.query(vista);cols=new String[]{"ID","Nombre","Teléfono","Correo","Rol","Activo","Usuario"};break;case "publicaciones":data=s.query(vista);cols=new String[]{"ID","Equipo","Título","Descripción","Tipo","Activa"};break;case "historial":data=filtro.isEmpty()?new Object[0][]:s.query(vista,Integer.parseInt(filtro),Integer.parseInt(filtro),Integer.parseInt(filtro));cols=new String[]{"Operación","Referencia","Fecha","Importe","Estado"};break;default:if(filtro.isEmpty())filtro=LocalDate.now()+","+LocalDate.now();String[] d=filtro.split(",");data=s.query(vista,d[0],d[1],d[0],d[1]);cols=new String[]{"Concepto","Ingresos"};}tabla.setModel(new DefaultTableModel(data,cols){public boolean isCellEditable(int r,int c){return false;}});}
}
