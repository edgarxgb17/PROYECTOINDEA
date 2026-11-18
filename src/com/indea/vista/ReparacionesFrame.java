package com.indea.vista;

import com.indea.controlador.ClienteController;
import com.indea.controlador.ReparacionController;
import com.indea.modelo.CambioEstado;
import com.indea.modelo.Cliente;
import com.indea.modelo.Empleado;
import com.indea.modelo.EstadoReparacion;
import com.indea.modelo.Reparacion;
import com.indea.modelo.Rol;
import com.indea.servicio.RefaccionService;
import com.indea.servicio.RefaccionService.Refaccion;
import com.indea.util.Sesion;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ButtonGroup;
import javax.swing.JToggleButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

public class ReparacionesFrame extends JPanel {

    private static final long serialVersionUID = 1L;

    private final ReparacionController controlador = new ReparacionController();
    private final ClienteController clienteController = new ClienteController();

    private final JTextField txtTelefonoCliente = new JTextField(22);
    private final JLabel lblCliente = new JLabel("Ningún cliente seleccionado");
    private final JComboBox<String> cmbTipo = new JComboBox<>(new String[] {"Laptop", "PC de escritorio", "All in one", "Impresora", "Monitor", "Otro"});
    private final JTextField txtMarca = new JTextField(20);
    private final JTextField txtModelo = new JTextField(20);
    private final JTextArea txtFalla = new JTextArea(4, 20);
    private final JComboBox<Empleado> cmbTecnico = new JComboBox<>();
    private Cliente clienteSeleccionado;

    private final JTextField txtBuscar = new JTextField(18);
    private final DefaultTableModel modelo = Ui.modeloTabla("Folio", "Cliente", "Equipo", "Estado", "Ingreso");
    private final JTable tabla = new JTable(modelo);
    private final List<Reparacion> mostradas = new ArrayList<>();
    private final JTextField txtFolioCopiable = new JTextField(15);
    private final JButton btnCopiarFolio = new JButton("Copiar folio");
    private final JLabel lblClienteFolio = new JLabel("Selecciona una reparación de la tabla");
    private final JComboBox<EstadoReparacion> cmbEstado = new JComboBox<>(EstadoReparacion.values());
    private final JTextField txtCosto = new JTextField(10);
    private final JCheckBox chkPagado = new JCheckBox("Servicio pagado");
    private final JTextArea txtDiagnostico = new JTextArea(4, 20);
    private final JTextArea txtHistorial = new JTextArea(6, 22);
    private final JButton btnGuardarCambios = new JButton("Guardar cambios");
    private final JButton btnRefacciones = new JButton("Refacciones del folio");
    private final RefaccionService servicioRefacciones = new RefaccionService();
    private boolean folioEntregado;
    private String folioSeleccionado;

    public ReparacionesFrame() {
        
        
        setLayout(new BorderLayout());
        add(Ui.encabezado("Reparaciones", "Recibir equipos, generar folio y dar seguimiento al servicio"), BorderLayout.NORTH);

        add(construirNavegacion(), BorderLayout.CENTER);

        cargarTecnicos();
        
        
        
        
        refrescarTabla();
        habilitarEdicion(false);
        btnRefacciones.setEnabled(false);
    }

    private JPanel construirNavegacion() {
        CardLayout cartas = new CardLayout();
        JPanel contenido = new JPanel(cartas);
        contenido.add(construirNueva(),"nueva");
        contenido.add(construirSeguimiento(),"seguimiento");
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT,8,7));
        barra.setBackground(new Color(239,244,250));
        ButtonGroup grupo = new ButtonGroup();
        JToggleButton nueva = new JToggleButton("Nueva reparación",true);
        JToggleButton seguimiento = new JToggleButton("Seguimiento");
        grupo.add(nueva); grupo.add(seguimiento);
        for (JToggleButton b : new JToggleButton[]{nueva,seguimiento}) {
            b.setUI(new javax.swing.plaf.basic.BasicToggleButtonUI());
            b.setOpaque(true);
            b.setContentAreaFilled(true);
            b.setBorderPainted(true);
            b.setFocusPainted(true);
            b.setFont(b.getFont().deriveFont(Font.BOLD,14f));
            b.addItemListener(e -> pintarBoton(b));
            pintarBoton(b);
            barra.add(b);
        }
        nueva.addActionListener(e -> cartas.show(contenido,"nueva"));
        seguimiento.addActionListener(e -> { cartas.show(contenido,"seguimiento"); refrescarSinPerderFolio(); });
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.add(barra,BorderLayout.NORTH);
        raiz.add(contenido,BorderLayout.CENTER);
        return raiz;
    }

    private void refrescarSinPerderFolio() {
        String anterior = folioSeleccionado;
        refrescarTabla();
        if (anterior==null) return;
        for (int i=0;i<mostradas.size();i++) {
            if (anterior.equals(mostradas.get(i).getFolio())) { tabla.setRowSelectionInterval(i,i); break; }
        }
    }

    private static void pintarBoton(JToggleButton boton) {
        boton.setForeground(boton.isSelected()?Color.WHITE:Ui.AZUL);
        boton.setBackground(boton.isSelected()?Ui.AZUL:Color.WHITE);
        boton.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Ui.AZUL,1,true),
                BorderFactory.createEmptyBorder(7,15,7,15)));
    }

    private JPanel construirNueva() {
        JPanel form = Ui.formulario();
        form.setBorder(BorderFactory.createTitledBorder("Datos del equipo recibido"));

        JPanel filaCliente = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        JButton btnBuscarCliente = new JButton("Buscar");
        JButton btnNuevoCliente = new JButton("Registrar cliente nuevo");
        filaCliente.add(txtTelefonoCliente);
        filaCliente.add(btnBuscarCliente);
        filaCliente.add(btnNuevoCliente);

        cmbTipo.setEditable(true);
        txtFalla.setLineWrap(true);
        txtFalla.setWrapStyleWord(true);

        Ui.fila(form, 0, "Buscar cliente (nombre o teléfono) *", filaCliente);
        Ui.fila(form, 1, "Cliente", lblCliente);
        Ui.fila(form, 2, "Tipo de equipo *", cmbTipo);
        Ui.fila(form, 3, "Marca", txtMarca);
        Ui.fila(form, 4, "Modelo", txtModelo);
        Ui.fila(form, 5, "Falla reportada *", new JScrollPane(txtFalla));
        Ui.fila(form, 6, "Técnico asignado *", cmbTecnico);

        JButton btnRegistrar = new JButton("Registrar y generar folio");
        JButton btnLimpiar = new JButton("Limpiar");
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botones.add(btnRegistrar);
        botones.add(btnLimpiar);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(form, BorderLayout.NORTH);
        panel.add(botones, BorderLayout.CENTER);

        btnBuscarCliente.addActionListener(e -> buscarCliente());
        txtTelefonoCliente.addActionListener(e -> buscarCliente());
        btnNuevoCliente.addActionListener(e -> PanelPrincipalFrame.navegarDesde(this, "Clientes"));
        btnRegistrar.addActionListener(e -> registrar());
        btnLimpiar.addActionListener(e -> limpiarNueva());
        return panel;
    }

    private JPanel construirSeguimiento() {
        JPanel busqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnBuscar = new JButton("Buscar");
        JButton btnTodas = new JButton("Ver todas");
        busqueda.add(new JLabel("Folio, cliente o teléfono:"));
        busqueda.add(txtBuscar);
        busqueda.add(btnBuscar);
        busqueda.add(btnTodas);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setFillsViewportHeight(true);
        tabla.setSelectionBackground(new Color(217,232,250));
        tabla.setSelectionForeground(new Color(23,41,66));
        tabla.setRowHeight(Math.max(22,tabla.getRowHeight()));

        txtDiagnostico.setLineWrap(true);
        txtDiagnostico.setWrapStyleWord(true);
        txtHistorial.setEditable(false);
        txtHistorial.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JPanel izquierda = Ui.formulario();
        izquierda.setBorder(BorderFactory.createTitledBorder("Actualizar reparación"));
        txtFolioCopiable.setEditable(false);
        txtFolioCopiable.setBackground(Color.WHITE);
        txtFolioCopiable.setToolTipText("Selecciona el texto o usa Copiar folio.");
        btnCopiarFolio.setEnabled(false);
        btnCopiarFolio.addActionListener(e -> copiarFolio());
        JPanel casilla = new JPanel(new BorderLayout(6,0));
        casilla.add(txtFolioCopiable,BorderLayout.CENTER);
        casilla.add(btnCopiarFolio,BorderLayout.EAST);
        Ui.fila(izquierda,0,"Folio",casilla);
        Ui.fila(izquierda,1,"Cliente",lblClienteFolio);
        Ui.fila(izquierda,2,"Estado",cmbEstado);
        Ui.fila(izquierda,3,"Costo del servicio",txtCosto);
        Ui.fila(izquierda,4,"",chkPagado);
        Ui.fila(izquierda,5,"Diagnóstico",new JScrollPane(txtDiagnostico));

        JPanel derecha = new JPanel(new BorderLayout());
        derecha.setBorder(BorderFactory.createTitledBorder("Historial de estados"));
        derecha.add(new JScrollPane(txtHistorial), BorderLayout.CENTER);

        JPanel detalle = new JPanel(new GridLayout(1, 2, 8, 0));
        detalle.add(izquierda);
        detalle.add(derecha);

        JPanel sur = new JPanel(new BorderLayout());
        sur.add(detalle, BorderLayout.CENTER);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botones.add(btnGuardarCambios);
        botones.add(btnRefacciones);
        sur.add(botones, BorderLayout.SOUTH);

        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(busqueda, BorderLayout.NORTH);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        panel.add(sur, BorderLayout.SOUTH);

        btnBuscar.addActionListener(e -> refrescarTabla());
        txtBuscar.addActionListener(e -> refrescarTabla());
        btnTodas.addActionListener(e -> {
            txtBuscar.setText("");
            refrescarTabla();
        });
        btnGuardarCambios.addActionListener(e -> guardarCambios());
        btnRefacciones.addActionListener(e -> mostrarRefacciones());
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccion();
            }
        });
        return panel;
    }

    private void cargarTecnicos() {
        try {
            cmbTecnico.removeAllItems();
            for (Empleado t : controlador.listarTecnicos()) {
                cmbTecnico.addItem(t);
            }
            if (Sesion.getRol() == Rol.TECNICO) {
                for (int i = 0; i < cmbTecnico.getItemCount(); i++) {
                    if (cmbTecnico.getItemAt(i).getId() == Sesion.getEmpleado().getId()) {
                        cmbTecnico.setSelectedIndex(i);
                    }
                }
                cmbTecnico.setEnabled(false);
            }
        } catch (RuntimeException ex) {
            Ui.manejar(this, ex);
        }
    }

    private void buscarCliente() {
        try {
            String criterio = txtTelefonoCliente.getText().trim();
            if (criterio.isEmpty()) {
                Ui.aviso(this, "Escribe el nombre o teléfono del cliente.");
                txtTelefonoCliente.requestFocusInWindow();
                return;
            }

            // Coincidencia exacta por teléfono; si no existe, búsqueda parcial por nombre/teléfono.
            Cliente exacto = criterio.matches("[0-9() -]+")
                    ? clienteController.buscarPorTelefono(criterio) : null;
            if (exacto != null) {
                seleccionarCliente(exacto);
                return;
            }

            List<Cliente> coincidencias = clienteController.buscar(criterio);
            if (coincidencias.isEmpty()) {
                clienteSeleccionado = null;
                lblCliente.setText("Cliente no encontrado. Usa \"Registrar cliente nuevo\".");
                Ui.aviso(this, "No se encontraron clientes con ese nombre o teléfono.");
            } else if (coincidencias.size() == 1) {
                seleccionarCliente(coincidencias.get(0));
            } else {
                Object elegido = javax.swing.JOptionPane.showInputDialog(
                        this, "Se encontraron varios clientes. Selecciona uno:",
                        "Seleccionar cliente", javax.swing.JOptionPane.QUESTION_MESSAGE,
                        null, coincidencias.toArray(), coincidencias.get(0));
                if (elegido instanceof Cliente) {
                    seleccionarCliente((Cliente) elegido);
                }
            }
        } catch (RuntimeException ex) {
            Ui.manejar(this, ex);
        }
    }

    private void seleccionarCliente(Cliente c) {
        clienteSeleccionado = c;
        txtTelefonoCliente.setText(c.getNombre());
        lblCliente.setText(c.getNombre() + "  •  " + (c.getTelefono() != null
                ? "Tel. " + c.getTelefono() : "Correo: " + c.getCorreo()));
    }

    private void registrar() {
        try {
            Object tipo = cmbTipo.getSelectedItem();
            Reparacion r = controlador.registrar(clienteSeleccionado, (Empleado) cmbTecnico.getSelectedItem(),
                    tipo == null ? "" : tipo.toString(), txtMarca.getText(), txtModelo.getText(), txtFalla.getText());
            mostrarRecibo(r);
            limpiarNueva();
            refrescarTabla();
        } catch (RuntimeException ex) {
            Ui.manejar(this, ex);
        }
    }

    private void mostrarRecibo(Reparacion r) {
        StringBuilder sb = new StringBuilder();
        sb.append("INDEA - RECIBO DE EQUIPO\n");
        sb.append("------------------------------------\n");
        sb.append("Folio:    ").append(r.getFolio()).append("\n");
        sb.append("Fecha:    ").append(r.getFechaIngreso().format(Ui.FECHA_HORA)).append("\n");
        sb.append("Cliente:  ").append(r.getCliente().getNombre()).append("\n");
        Cliente cliente = r.getCliente();
        if (cliente.getTelefono() != null) {
            sb.append("Teléfono: ").append(cliente.getTelefono()).append("\n");
        } else if (cliente.getCorreo() != null) {
            sb.append("Correo:   ").append(cliente.getCorreo()).append("\n");
        }
        sb.append("Equipo:   ").append(r.getEquipoDescripcion()).append("\n");
        sb.append("Falla:    ").append(r.getFallaReportada()).append("\n");
        sb.append("Técnico:  ").append(r.getTecnico().getNombre()).append("\n");
        sb.append("Estado:   ").append(r.getEstado().getEtiqueta()).append("\n");
        sb.append("------------------------------------\n");
        sb.append("Conserva este folio para consultar el avance.");
        JTextArea area = new JTextArea(sb.toString());
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        javax.swing.JOptionPane.showMessageDialog(this, area, "Reparación registrada", javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }

    private void limpiarNueva() {
        clienteSeleccionado = null;
        txtTelefonoCliente.setText("");
        lblCliente.setText("Ningún cliente seleccionado");
        cmbTipo.setSelectedIndex(0);
        txtMarca.setText("");
        txtModelo.setText("");
        txtFalla.setText("");
    }

    private void refrescarTabla() {
        try {
            List<Reparacion> lista = controlador.listar(txtBuscar.getText());
            modelo.setRowCount(0);
            mostradas.clear();
            for (Reparacion r : lista) {
                mostradas.add(r);
                modelo.addRow(new Object[] {r.getFolio(), r.getCliente().getNombre(), r.getEquipoDescripcion(),
                        r.getEstado().getEtiqueta(), r.getFechaIngreso().format(Ui.FECHA_HORA)});
            }
        } catch (RuntimeException ex) {
            Ui.manejar(this, ex);
        }
    }

    private void cargarSeleccion() {
        int fila = tabla.getSelectedRow();
        folioSeleccionado = null;
        btnRefacciones.setEnabled(false);
        txtFolioCopiable.setText("");
        btnCopiarFolio.setEnabled(false);
        lblClienteFolio.setText("Selecciona una reparación de la tabla");
        if (fila < 0 || fila >= mostradas.size()) {
            habilitarEdicion(false);
            return;
        }
        try {
            Reparacion r = controlador.consultarPorFolio(mostradas.get(fila).getFolio());
            folioSeleccionado = r.getFolio();
            folioEntregado = r.getEstado() == EstadoReparacion.ENTREGADO;
            btnRefacciones.setEnabled(true);
            txtFolioCopiable.setText(r.getFolio());
            btnCopiarFolio.setEnabled(true);
            lblClienteFolio.setText(r.getCliente().getNombre());
            cmbEstado.setSelectedItem(r.getEstado());
            txtCosto.setText(r.getCostoServicio().toPlainString());
            chkPagado.setSelected(r.isPagado());
            txtDiagnostico.setText(r.getDiagnostico() == null ? "" : r.getDiagnostico());
            StringBuilder sb = new StringBuilder();
            for (CambioEstado c : r.getHistorial()) {
                sb.append(c.getFechaCambio().format(Ui.FECHA_HORA)).append("  ").append(c.getEstado().getEtiqueta())
                        .append("\n   por ").append(c.getEmpleado().getNombre()).append("\n");
            }
            txtHistorial.setText(sb.toString());
            txtHistorial.setCaretPosition(0);
            habilitarEdicion(r.getEstado() != EstadoReparacion.ENTREGADO);
        } catch (RuntimeException ex) {
            Ui.manejar(this, ex);
        }
    }

    private void copiarFolio() {
        if (folioSeleccionado==null) return;
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(folioSeleccionado),null);
            Ui.info(this,"Folio copiado: "+folioSeleccionado);
        } catch (IllegalStateException e) {
            txtFolioCopiable.requestFocusInWindow();
            txtFolioCopiable.selectAll();
            Ui.aviso(this,"El portapapeles no está disponible. Puedes copiar el folio seleccionándolo.");
        }
    }

    private void mostrarRefacciones() {
        String folio = folioSeleccionado;
        if (folio == null) return;
        try {
            while (true) {
                DefaultTableModel detalle = Ui.modeloTabla("Refacción", "Cantidad", "Costo unitario", "Subtotal");
                for (var pieza : servicioRefacciones.listarPorFolio(folio)) {
                    detalle.addRow(new Object[] {pieza.nombre(), pieza.cantidad(), pieza.costoUnitario(),
                            pieza.costoUnitario().multiply(java.math.BigDecimal.valueOf(pieza.cantidad()))});
                }
                JTable piezas = new JTable(detalle);
                piezas.setFillsViewportHeight(true);
                JScrollPane conteo = new JScrollPane(piezas);
                conteo.setPreferredSize(new Dimension(525, 180));
                JPanel contenido = new JPanel(new BorderLayout(0, 6));
                contenido.add(new JLabel(detalle.getRowCount() == 0 ? "Todavía no hay piezas asignadas."
                        : "Piezas utilizadas en el folio. El costo del servicio se captura aparte."), BorderLayout.NORTH);
                contenido.add(conteo, BorderLayout.CENTER);
                String[] acciones = folioEntregado ? new String[]{"Cerrar"} : new String[]{"Agregar refacción", "Cerrar"};
                int accion = JOptionPane.showOptionDialog(this, contenido, "Refacciones · " + folio,
                        JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, acciones, "Cerrar");
                if (folioEntregado || accion != 0) return;
                try { agregarRefaccion(folio); } catch (RuntimeException e) { Ui.manejar(this, e); }
            }
        } catch (RuntimeException e) {
            Ui.manejar(this, e);
        }
    }

    private void agregarRefaccion(String folio) {
        List<SelectorOperacion.Opcion<Refaccion>> opciones = new ArrayList<>();
        for (Refaccion pieza : servicioRefacciones.listar()) {
            if (pieza.stock() > 0) {
                opciones.add(new SelectorOperacion.Opcion<>(pieza, pieza.nombre() + " · " + pieza.stock()
                        + " disponibles · costo " + pieza.costo().toPlainString(), null));
            }
        }
        if (opciones.isEmpty()) {
            Ui.aviso(this, "No hay refacciones con existencias. El administrador puede registrarlas en Inventario → Refacciones.");
            return;
        }
        Refaccion pieza = SelectorOperacion.abrir(this, "Agregar refacción al folio " + folio,
                "Buscar por nombre de la pieza", opciones, null, null);
        if (pieza == null) return;
        String cantidad = JOptionPane.showInputDialog(this, "Unidades que se usarán de " + pieza.nombre(), "1");
        if (cantidad == null) return;
        servicioRefacciones.asignar(folio, pieza.id(), cantidad);
    }

    private void guardarCambios() {
        if (folioSeleccionado == null) {
            Ui.aviso(this, "Selecciona una reparación de la tabla.");
            return;
        }
        try {
            controlador.actualizar(folioSeleccionado, (EstadoReparacion) cmbEstado.getSelectedItem(),
                    txtDiagnostico.getText(), txtCosto.getText(), chkPagado.isSelected());
            Ui.info(this, "Reparación " + folioSeleccionado + " actualizada.");
            int fila = tabla.getSelectedRow();
            refrescarTabla();
            if (fila >= 0 && fila < tabla.getRowCount()) {
                tabla.setRowSelectionInterval(fila, fila);
            }
            cargarSeleccion();
        } catch (RuntimeException ex) {
            Ui.manejar(this, ex);
        }
    }

    private void habilitarEdicion(boolean activa) {
        cmbEstado.setEnabled(activa);
        txtCosto.setEnabled(activa);
        chkPagado.setEnabled(activa);
        txtDiagnostico.setEnabled(activa);
        btnGuardarCambios.setEnabled(activa);
    }
}
