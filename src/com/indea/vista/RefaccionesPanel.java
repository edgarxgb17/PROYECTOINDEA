package com.indea.vista;

import com.indea.modelo.Rol;
import com.indea.servicio.RefaccionService;
import com.indea.servicio.ReportesPdfService;
import com.indea.servicio.RefaccionService.Refaccion;
import com.indea.util.Sesion;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

/** Pestaña de piezas, independiente del inventario de equipos para venta. */
final class RefaccionesPanel extends JPanel {
    private final RefaccionService servicio = new RefaccionService();
    private final DefaultTableModel modelo = Ui.modeloTabla("Refacción", "Costo unitario", "Existencias");
    private final JTable tabla = new JTable(modelo);
    private final JLabel estado = new JLabel(" ");
    private List<Refaccion> visibles = List.of();

    RefaccionesPanel() {
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoCreateRowSorter(true);
        tabla.setFillsViewportHeight(true);
        add(new JLabel("Piezas que se utilizan en las reparaciones."), BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        JPanel pie = new JPanel(new BorderLayout());
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        if (Sesion.getRol() == Rol.ADMINISTRADOR) {
            JButton nueva = new JButton("Nueva refacción");
            JButton entrada = new JButton("Agregar existencias");
            entrada.setEnabled(false);
            tabla.getSelectionModel().addListSelectionListener(e -> entrada.setEnabled(tabla.getSelectedRow() >= 0));
            nueva.addActionListener(e -> hacer(this::registrar));
            entrada.addActionListener(e -> hacer(this::reponer));
            acciones.add(nueva);
            acciones.add(entrada);
        }
        JButton actualizar = new JButton("Actualizar");
        actualizar.addActionListener(e -> hacer(this::refrescar));
        acciones.add(actualizar);
        JButton btnPdf = new JButton("PDF de refacciones");
        btnPdf.addActionListener(e -> ExportarPdf.guardar(this,"INDEA-refacciones",ReportesPdfService::refacciones));
        acciones.add(btnPdf);
        pie.add(acciones, BorderLayout.NORTH);
        pie.add(estado, BorderLayout.SOUTH);
        add(pie, BorderLayout.SOUTH);
    }

    void refrescar() {
        visibles = servicio.listar();
        modelo.setRowCount(0);
        for (Refaccion pieza : visibles) modelo.addRow(new Object[]{pieza.nombre(), pieza.costo(), pieza.stock()});
        estado.setText(visibles.isEmpty() ? "Todavía no hay refacciones registradas."
                : visibles.size() + " refacción(es) en el catálogo.");
    }

    private void registrar() {
        JTextField nombre = new JTextField(25), costo = new JTextField(15), piezas = new JTextField("0", 10);
        JPanel campos = new JPanel(new GridLayout(0, 2, 8, 8));
        campos.add(new JLabel("Nombre de la pieza")); campos.add(nombre);
        campos.add(new JLabel("Costo por pieza")); campos.add(costo);
        campos.add(new JLabel("Existencias iniciales")); campos.add(piezas);
        while (JOptionPane.showConfirmDialog(this, campos, "Nueva refacción",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            try {
                servicio.registrar(nombre.getText(), costo.getText(), piezas.getText());
                break;
            } catch (RuntimeException e) {
                Ui.manejar(this, e);
            }
        }
        refrescar();
    }

    private void reponer() {
        int seleccion = tabla.getSelectedRow();
        if (seleccion < 0) return;
        Refaccion pieza = visibles.get(tabla.convertRowIndexToModel(seleccion));
        String unidades = JOptionPane.showInputDialog(this, "Unidades que ingresan de " + pieza.nombre(), "1");
        if (unidades == null) return;
        servicio.reponer(pieza.id(), unidades);
        refrescar();
    }

    private void hacer(Runnable accion) {
        try { accion.run(); } catch (RuntimeException e) { Ui.manejar(this, e); }
    }
}
