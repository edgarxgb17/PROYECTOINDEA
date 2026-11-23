package com.indea.vista;

import com.indea.controlador.AutenticacionController;
import com.indea.controlador.ClienteController;
import com.indea.controlador.ReparacionController;
import com.indea.modelo.Rol;
import com.indea.util.Sesion;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class PanelPrincipalFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private final JPanel contenido = new JPanel(new java.awt.CardLayout());
    private final java.util.Map<String,JPanel> vistas = new java.util.HashMap<>();
    private final java.util.List<String> historial = new java.util.ArrayList<>();
    private final java.util.Map<String,BotonIndea> enlaces = new java.util.HashMap<>();

    private BotonIndea enlace(String texto, String destino) {
        BotonIndea boton = new BotonIndea(texto);
        boton.comoMenu();
        enlaces.put(destino, boton);
        return boton;
    }

    private int posicion = -1;
    private final JButton atras = new BotonIndea("← Atrás"), adelante = new BotonIndea("Adelante →");
    private final JLabel ruta = new JLabel("Inicio");
    static void navegarDesde(java.awt.Component origen, String destino) {
        java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(origen);
        if (w instanceof PanelPrincipalFrame) ((PanelPrincipalFrame)w).navegar(destino);
    }
    private void navegar(String destino) {
        try {
            if (!vistas.containsKey(destino)) {
                JPanel p;
                if (destino.equals("Clientes")) p = new ClientesFrame();
                else if (destino.equals("Reparaciones")) p = new ReparacionesFrame();
                else if (destino.equals("Consultar folio")) p = new ConsultaFolioPanel();
                else p = new NegocioFrame(destino);
                vistas.put(destino,p); contenido.add(p,destino);
            }
            if (posicion >= 0 && historial.get(posicion).equals(destino)) return;
            while (historial.size() > posicion+1) historial.remove(historial.size()-1);
            historial.add(destino); posicion++; mostrar();
        } catch (RuntimeException ex) { Ui.manejar(this,ex); }
    }
    private void mostrar() {
        String destino = historial.get(posicion);
        ((java.awt.CardLayout)contenido.getLayout()).show(contenido,destino);
        enlaces.forEach((nombre, boton) -> boton.activar(nombre.equals(destino)));
        ruta.setText(destino.substring(0,1).toUpperCase() + destino.substring(1)); atras.setEnabled(posicion>0); adelante.setEnabled(posicion<historial.size()-1);
        if (vistas.get(destino) instanceof NegocioFrame) ((NegocioFrame)vistas.get(destino)).refresh();
        if (vistas.get(destino) instanceof ReparacionesFrame) ((ReparacionesFrame)vistas.get(destino)).refrescarAlMostrar();
        if (destino.equals("Inicio")) actualizarResumen(Sesion.getRol()==Rol.ADMINISTRADOR || Sesion.getRol()==Rol.TECNICO,true);
    }

    private final ClienteController clienteController = new ClienteController();
    private final ReparacionController reparacionController = new ReparacionController();
    private final AutenticacionController autenticacionController = new AutenticacionController();
    private final JLabel lblReparaciones = new JLabel("-", SwingConstants.CENTER);
    private final JLabel lblInventario = new JLabel("-", SwingConstants.CENTER);
    private final JLabel lblVentas = new JLabel("-", SwingConstants.CENTER);
    private final JLabel lblIngresos = new JLabel("-", SwingConstants.CENTER);
    private final JLabel lblClientes = new JLabel("-", SwingConstants.CENTER);

    public PanelPrincipalFrame() {
        Rol rol = Sesion.getRol();
        setIconImage(Ui.LOGO.getImage());
        setTitle("INDEA - Panel principal");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());
        JPanel cabecera = new JPanel(new BorderLayout(14, 0));
        cabecera.setBackground(Color.WHITE);
        cabecera.setBorder(BorderFactory.createEmptyBorder(13, 19, 13, 19));
        JLabel titulo = new JLabel("Panel principal");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 22f));
        cabecera.add(titulo, BorderLayout.WEST);
        cabecera.add(new JLabel(Sesion.getEmpleado().getNombre() + "  ·  " + rol.getEtiqueta(),
                SwingConstants.RIGHT), BorderLayout.CENTER);

        JPanel centro = new JPanel(new BorderLayout(0, 16));
        centro.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel resumen = new JPanel(new GridLayout(1, 2, 12, 0));
        boolean veReparaciones = rol == Rol.ADMINISTRADOR || rol == Rol.TECNICO;
        boolean veClientes = true;
        if (veReparaciones) {
            resumen.add(tarjeta("Equipos en reparación", lblReparaciones));
        }
        if (veClientes) {
            resumen.add(tarjeta("Clientes registrados", lblClientes));
        }
        if (!veReparaciones && !veClientes) {
            JLabel aviso = new JLabel("Las publicaciones de marketing estarán disponibles en los próximos sprints.", SwingConstants.CENTER);
            resumen.add(aviso);
        }
        if (rol == Rol.ADMINISTRADOR) { resumen.add(tarjeta("Disponibles",lblInventario)); resumen.add(tarjeta("Ventas del mes",lblVentas)); resumen.add(tarjeta("Ingresos totales",lblIngresos)); }
        JPanel portada = new JPanel(new BorderLayout(0, 14));
        portada.add(Ui.encabezado("Resumen de actividad", "Sistema de gestión INDEA"), BorderLayout.NORTH);
        portada.add(resumen, BorderLayout.CENTER);
        centro.add(portada, BorderLayout.NORTH);

        JPanel menu = new JPanel(new GridLayout(0, 1, 0, 8));
        JButton btnClientes = enlace("Clientes", "Clientes");
        JButton btnReparaciones = enlace("Reparaciones", "Reparaciones");
        JButton btnFolio = enlace("Consultar reparación por folio", "Consultar folio");
        JButton btnSalir = enlace("Cerrar sesión", "Salir");
        btnClientes.setEnabled(veClientes);
        btnReparaciones.setEnabled(veReparaciones);
        menu.add(btnClientes);
        menu.add(btnReparaciones);
        menu.add(btnFolio);
        for (String modulo : new String[]{"inventario","empleados","publicaciones","historial","reportes"}) {
            if (modulo.equals("empleados") || modulo.equals("reportes")) { if (rol != Rol.ADMINISTRADOR) continue; }
            if (modulo.equals("publicaciones") && rol == Rol.TECNICO) continue;
            if (modulo.equals("historial") && rol == Rol.MARKETING) continue;
            JButton b = enlace(modulo.substring(0,1).toUpperCase()+modulo.substring(1), modulo);
            menu.add(b); b.addActionListener(e -> {try {navegar(modulo);} catch(RuntimeException ex){Ui.manejar(this,ex);}});
        }
        menu.add(btnSalir);
        JButton inicio = enlace("Inicio", "Inicio"); inicio.addActionListener(e -> navegar("Inicio"));
        menu.add(inicio,0);
        JPanel lateral = new JPanel(new BorderLayout());
        lateral.setBackground(Ui.AZUL);
        lateral.setBorder(BorderFactory.createEmptyBorder(14, 9, 10, 9));
        JLabel logo = new JLabel(new javax.swing.ImageIcon(Ui.LOGO.getImage()
                .getScaledInstance(124, 84, java.awt.Image.SCALE_SMOOTH)));
        logo.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        lateral.add(logo, BorderLayout.NORTH);
        menu.setOpaque(false);
        JPanel lista = new JPanel(new BorderLayout());
        lista.setBackground(Ui.AZUL);
        lista.add(menu, BorderLayout.NORTH);
        javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(lista);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        Ui.ajustarDesplazamiento(scroll);
        lateral.add(scroll, BorderLayout.CENTER);
        add(lateral,BorderLayout.WEST);
        vistas.put("Inicio",centro); contenido.add(centro,"Inicio");
        JPanel cuerpo = new JPanel(new BorderLayout());
        JPanel barra = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        barra.add(atras); barra.add(adelante); barra.add(ruta);
        atras.addActionListener(e -> {if(posicion>0){posicion--;mostrar();}});
        adelante.addActionListener(e -> {if(posicion<historial.size()-1){posicion++;mostrar();}});
        barra.setBorder(BorderFactory.createEmptyBorder(3, 9, 3, 9));
        JPanel superior = new JPanel(new BorderLayout());
        superior.add(cabecera, BorderLayout.NORTH);
        superior.add(barra, BorderLayout.SOUTH);
        cuerpo.add(superior,BorderLayout.NORTH); cuerpo.add(contenido,BorderLayout.CENTER); add(cuerpo,BorderLayout.CENTER);
        navegar("Inicio");

        btnClientes.addActionListener(e -> navegar("Clientes"));
        btnReparaciones.addActionListener(e -> navegar("Reparaciones"));
        btnFolio.addActionListener(e -> navegar("Consultar folio"));
        btnSalir.addActionListener(e -> cerrarSesion());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (confirmarSalida("¿Deseas cerrar INDEA?")) {
                    autenticacionController.cerrarSesion();
                    dispose();
                }
            }
            @Override
            public void windowActivated(WindowEvent e) {
                actualizarResumen(veReparaciones, veClientes);
            }
        });

        setPreferredSize(new Dimension(1280, 800));
        setMinimumSize(new Dimension(760, 520));
        setResizable(true);
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel tarjeta(String titulo, JLabel valor) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createLineBorder(Ui.LINEA));
        valor.setForeground(Ui.ACCION);
        valor.setFont(valor.getFont().deriveFont(Font.BOLD, 28f));
        JLabel t = new JLabel(titulo, SwingConstants.CENTER);
        t.setBorder(BorderFactory.createEmptyBorder(8, 4, 0, 4));
        p.add(t, BorderLayout.NORTH);
        p.add(valor, BorderLayout.CENTER);
        p.setBorder(BorderFactory.createCompoundBorder(p.getBorder(), BorderFactory.createEmptyBorder(0, 0, 8, 0)));
        return p;
    }

    private void actualizarResumen(boolean reparaciones, boolean clientes) {
        try {
            if (Sesion.getRol() == Rol.ADMINISTRADOR) { Object[] r = new com.indea.servicio.NegocioService().query("panel")[0]; lblInventario.setText(r[1].toString()); lblVentas.setText(r[2].toString()); lblIngresos.setText(r[3].toString()); }
            if (reparaciones) {
                lblReparaciones.setText(String.valueOf(reparacionController.contarActivas()));
            }
            if (clientes) {
                lblClientes.setText(String.valueOf(clienteController.contar()));
            }
        } catch (RuntimeException ex) {
            Ui.manejar(this, ex);
        }
    }

    private boolean confirmarSalida(String mensaje) {
        Object[] botones = {"Cancelar", "Sí, salir"};
        return javax.swing.JOptionPane.showOptionDialog(this, mensaje,
                "Confirmar salida", javax.swing.JOptionPane.DEFAULT_OPTION,
                javax.swing.JOptionPane.QUESTION_MESSAGE, null, botones, botones[0]) == 1;
    }

    private void cerrarSesion() {
        if (!confirmarSalida("¿Quieres cerrar tu sesión y regresar a la pantalla de acceso?")) return;
        autenticacionController.cerrarSesion();
        new LoginFrame().setVisible(true);
        dispose();
    }
}
