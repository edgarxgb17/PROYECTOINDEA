package com.indea.vista;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

/** Búsqueda y selección de un registro sin capturar su identificador. */
final class SelectorOperacion {
    private SelectorOperacion() {}

    record Opcion<T>(T valor, String etiqueta, String telefono) {
        @Override public String toString() { return etiqueta; }
    }

    static <T> T abrir(Component padre, String titulo, String pista, List<Opcion<T>> opciones,
                        String textoAlta, Supplier<T> alta) {
        Window dueno = SwingUtilities.getWindowAncestor(padre);
        JDialog dialogo = new JDialog(dueno, titulo, Dialog.ModalityType.APPLICATION_MODAL);
        dialogo.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        JTextField buscar = new JTextField(28);
        JLabel etiqueta = new JLabel(pista);
        etiqueta.setLabelFor(buscar);
        JButton seleccionar = new BotonIndea("Seleccionar", true);
        JButton cancelar = new BotonIndea("Cancelar");
        DefaultListModel<Opcion<T>> modelo = new DefaultListModel<>();
        JList<Opcion<T>> lista = new JList<>(modelo);
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lista.addListSelectionListener(e -> seleccionar.setEnabled(lista.getSelectedValue() != null));
        JLabel estado = new JLabel();
        Runnable filtrar = () -> {
            modelo.clear();
            for (Opcion<T> opcion : opciones) {
                if (coincide(opcion.etiqueta(), opcion.telefono(), buscar.getText())) modelo.addElement(opcion);
            }
            seleccionar.setEnabled(false);
            estado.setText(modelo.isEmpty() ? "Sin coincidencias" : modelo.size() + " resultado(s)");
        };
        buscar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filtrar.run(); }
            public void removeUpdate(DocumentEvent e) { filtrar.run(); }
            public void changedUpdate(DocumentEvent e) { filtrar.run(); }
        });
        filtrar.run();

        var resultado = new java.util.concurrent.atomic.AtomicReference<T>();
        seleccionar.addActionListener(e -> {
            Opcion<T> elegida = lista.getSelectedValue();
            if (elegida != null) { resultado.set(elegida.valor()); dialogo.dispose(); }
        });
        lista.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int indice = lista.locationToIndex(e.getPoint());
                if (e.getClickCount() == 2 && indice >= 0 && lista.getCellBounds(indice, indice).contains(e.getPoint())) {
                    seleccionar.doClick();
                }
            }
        });
        buscar.addActionListener(e -> {
            if (!modelo.isEmpty()) { lista.setSelectedIndex(0); lista.requestFocusInWindow(); }
        });
        dialogo.getRootPane().setDefaultButton(seleccionar);
        dialogo.getRootPane().registerKeyboardAction(e -> dialogo.dispose(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);
        cancelar.addActionListener(e -> dialogo.dispose());

        JPanel cabecera = new JPanel(new BorderLayout(0, 5));
        cabecera.add(etiqueta, BorderLayout.NORTH);
        cabecera.add(buscar, BorderLayout.CENTER);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        if (alta != null) {
            JButton nuevo = new BotonIndea(textoAlta);
            nuevo.addActionListener(e -> {
                T creada = alta.get();
                if (creada != null) { resultado.set(creada); dialogo.dispose(); }
            });
            acciones.add(nuevo);
        }
        acciones.add(seleccionar);
        acciones.add(cancelar);
        JPanel pie = new JPanel(new BorderLayout());
        pie.add(estado, BorderLayout.NORTH);
        pie.add(acciones, BorderLayout.SOUTH);
        JPanel cuerpo = new JPanel(new BorderLayout(0, 8));
        cuerpo.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
        cuerpo.add(cabecera, BorderLayout.NORTH);
        JScrollPane desplazamiento = new JScrollPane(lista);
        desplazamiento.setPreferredSize(new Dimension(510, 225));
        cuerpo.add(desplazamiento, BorderLayout.CENTER);
        cuerpo.add(pie, BorderLayout.SOUTH);
        dialogo.setContentPane(cuerpo);
        dialogo.pack();
        dialogo.setLocationRelativeTo(padre);
        SwingUtilities.invokeLater(buscar::requestFocusInWindow);
        dialogo.setVisible(true);
        return resultado.get();
    }

    static boolean coincide(String etiqueta, String telefono, String texto) {
        String filtro = normalizar(texto);
        if (normalizar(etiqueta).contains(filtro)) return true;
        String numeros = filtro.replaceAll("[ ()-]", "");
        return telefono != null && numeros.matches("[0-9]+")
                && telefono.replaceAll("[^0-9]", "").contains(numeros);
    }

    private static String normalizar(String texto) {
        return Normalizer.normalize(texto.strip(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT);
    }
}
