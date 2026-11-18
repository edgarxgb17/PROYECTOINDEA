package com.indea.vista;

import com.indea.servicio.ProveedorService;
import com.indea.servicio.ProveedorService.Ficha;
import com.indea.servicio.ProveedorService.Proveedor;
import java.awt.*;
import java.util.List;
import java.util.Objects;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

final class ProveedoresPanel extends JPanel {
    private final ProveedorService servicio = new ProveedorService();
    private final DefaultTableModel fichas = Ui.modeloTabla("Nombre comercial","Contacto","Teléfono","Correo","Dirección");
    private final DefaultTableModel compras = Ui.modeloTabla("Fecha","Equipo","Unidades","Costo unitario","Total");
    private final JTable tabla = new JTable(fichas);
    private final JLabel lblHistorial = new JLabel("Selecciona un proveedor para consultar sus compras.");
    private final JButton editar = new JButton("Editar proveedor");
    private List<Proveedor> lista = List.of();

    ProveedoresPanel() {
        setLayout(new BorderLayout(0,8));
        setBorder(BorderFactory.createEmptyBorder(10,12,8,12));
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoCreateRowSorter(true);
        tabla.setFillsViewportHeight(true);
        JTable historial = new JTable(compras);
        historial.setFillsViewportHeight(true);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton nuevo = new JButton("Nuevo proveedor"), actualizar = new JButton("Actualizar");
        editar.setEnabled(false);
        acciones.add(nuevo); acciones.add(editar); acciones.add(actualizar);
        JPanel inferior = new JPanel(new BorderLayout(0,5));
        inferior.setBorder(BorderFactory.createEmptyBorder(6,0,0,0));
        inferior.add(lblHistorial,BorderLayout.NORTH);
        inferior.add(new JScrollPane(historial),BorderLayout.CENTER);
        JSplitPane dividido = new JSplitPane(JSplitPane.VERTICAL_SPLIT,new JScrollPane(tabla),inferior);
        dividido.setResizeWeight(.55);
        dividido.setBorder(null);
        add(acciones,BorderLayout.NORTH);
        add(dividido,BorderLayout.CENTER);
        nuevo.addActionListener(e -> hacer(() -> {
            Ficha f = formulario(this,null,false);
            if (f != null) recargar(servicio.registrar(f));
        }));
        editar.addActionListener(e -> hacer(() -> {
            Proveedor p = seleccion();
            if (p == null) return;
            Ficha f = formulario(this,p.ficha(),false);
            if (f != null) { servicio.actualizar(p.id(),f); recargar(p.id()); }
        }));
        actualizar.addActionListener(e -> hacer(this::refrescar));
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) hacer(this::verCompras);
        });
    }

    void refrescar() {
        Proveedor anterior = seleccion();
        recargar(anterior == null ? 0 : anterior.id());
    }

    private void recargar(int id) {
        List<Proveedor> nuevos = servicio.listar();
        tabla.clearSelection();
        fichas.setRowCount(0);
        lista = nuevos;
        int elegido = -1;
        for (int i=0; i<lista.size(); i++) {
            Proveedor p = lista.get(i);
            Ficha f = p.ficha();
            fichas.addRow(new Object[]{f.nombre(),f.contacto(),f.telefono(),f.correo(),f.direccion()});
            if (p.id()==id) elegido=i;
        }
        if (elegido>=0) {
            int vista=tabla.convertRowIndexToView(elegido);
            tabla.setRowSelectionInterval(vista,vista);
        } else verCompras();
    }

    private Proveedor seleccion() {
        int fila=tabla.getSelectedRow();
        if (fila<0) return null;
        int i=tabla.convertRowIndexToModel(fila);
        return i<lista.size()?lista.get(i):null;
    }

    private void verCompras() {
        Proveedor p=seleccion();
        editar.setEnabled(p!=null);
        compras.setRowCount(0);
        if (p==null) {
            lblHistorial.setText(lista.isEmpty()?"Todavía no hay proveedores.":"Selecciona un proveedor para consultar sus compras.");
            return;
        }
        var filas=servicio.historial(p.id());
        lblHistorial.setText("Compras a "+p.ficha().nombre()+(filas.isEmpty()?" · Sin compras registradas":""));
        for (var c:filas) compras.addRow(new Object[]{c.fecha().toLocalDateTime().format(Ui.FECHA_HORA),
                c.equipo(),c.cantidad(),Ui.dinero(c.costoUnitario()),Ui.dinero(c.total())});
    }

    static Ficha formulario(Component origen,Ficha inicial,boolean dentroDeCompra) {
        String[] etiquetas={"Nombre comercial","Persona de contacto","Teléfono","Correo","Dirección"};
        String[] datos=inicial==null?new String[5]:new String[]{inicial.nombre(),inicial.contacto(),inicial.telefono(),inicial.correo(),inicial.direccion()};
        JTextField[] campos=new JTextField[5];
        JPanel formulario=Ui.formulario();
        for (int i=0;i<campos.length;i++) {
            campos[i]=new JTextField(Objects.toString(datos[i],""),31);
            Ui.fila(formulario,i,etiquetas[i],campos[i]);
        }
        JPanel cuerpo=new JPanel(new BorderLayout(0,8));
        cuerpo.add(new JLabel(dentroDeCompra?"La ficha se guardará al confirmar la compra.":"Solo el nombre comercial es obligatorio."),BorderLayout.NORTH);
        cuerpo.add(formulario,BorderLayout.CENTER);
        while (JOptionPane.showConfirmDialog(origen,cuerpo,inicial==null?"Registrar proveedor nuevo":"Editar proveedor",
                JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE)==JOptionPane.OK_OPTION) {
            try { return ProveedorService.preparar(campos[0].getText(),campos[1].getText(),
                    campos[2].getText(),campos[3].getText(),campos[4].getText()); }
            catch (RuntimeException e) { Ui.manejar(origen,e); }
        }
        return null;
    }

    private void hacer(Runnable accion) {
        try { accion.run(); } catch (RuntimeException e) { Ui.manejar(this,e); }
    }
}
