package com.indea.vista;

import com.indea.excepcion.AccesoDenegadoException;
import com.indea.excepcion.ReglaNegocioException;
import com.indea.excepcion.ValidacionException;

import javax.swing.ImageIcon;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.BorderLayout;
import java.time.format.DateTimeFormatter;

final class Ui {

    static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    static final Color AZUL = new Color(0x142B4D);
    static final Color ACCION = new Color(0x285DBB);
    static final Color TEXTO = Color.BLACK;
    static final Color FONDO = new Color(0xF0F4FA);
    static final Color LINEA = new Color(0xD5E0EE);
    static final Color SELECCION = new Color(0xD8E8FC);
    static final ImageIcon LOGO = new ImageIcon(Ui.class.getResource("imagenes/logo.png"));
    private static final ImageIcon MARCA = new ImageIcon(
            LOGO.getImage().getScaledInstance(74, 50, java.awt.Image.SCALE_SMOOTH));

    private Ui() {
    }

    static void configurarEstilo() {
        Font base = new Font("Segoe UI", Font.PLAIN, 15);
        for (Object key : UIManager.getLookAndFeelDefaults().keySet()) {
            if (key.toString().endsWith(".font")) UIManager.put(key, base);
        }
        UIManager.put("OptionPane.messageFont", base);
        Font fuerte = base.deriveFont(Font.BOLD);
        Font lectura = new Font("Segoe UI Semibold", Font.PLAIN, 15);
        if ("Dialog".equals(lectura.getFamily())) lectura = fuerte;
        UIManager.put("Label.font", fuerte);
        UIManager.put("Table.font", lectura);
        UIManager.put("Button.font", fuerte);
        UIManager.put("List.foreground", Color.BLACK);
        UIManager.put("ComboBox.foreground", Color.BLACK);
        UIManager.put("List.font", lectura);
        UIManager.put("TabbedPane.tabInsets", new Insets(7, 12, 7, 12));
        UIManager.put("ScrollPane.border", BorderFactory.createLineBorder(LINEA));
        UIManager.put("Panel.background", FONDO);
        UIManager.put("OptionPane.background", FONDO);
        UIManager.put("Viewport.background", Color.WHITE);
        UIManager.put("Label.foreground", TEXTO);
        UIManager.put("TitledBorder.titleColor", AZUL);
        UIManager.put("Button.background", Color.WHITE);
        UIManager.put("Button.foreground", AZUL);
        UIManager.getLookAndFeelDefaults().remove("Button.gradient");
        // Los botones nativos de JOptionPane usan el mismo estilo Metal,
        // pero con un indicador de foco en el borde exterior, no en el texto.
        UIManager.put("ButtonUI", BotonNativoUI.class.getName());
        UIManager.put("Button.focus", ACCION);
        UIManager.put("Button.select", SELECCION);
        UIManager.put("TabbedPane.font", base.deriveFont(Font.BOLD, 13f));
        UIManager.put("TabbedPane.background", FONDO);
        UIManager.put("TabbedPane.selected", new Color(0xDCEBFF));
        UIManager.put("TabbedPane.contentAreaColor", Color.WHITE);
        UIManager.put("TabbedPane.foreground", TEXTO);
        UIManager.put("Table.foreground", TEXTO);
        UIManager.put("Table.gridColor", LINEA);
        UIManager.put("Table.selectionBackground", SELECCION);
        UIManager.put("Table.selectionForeground", TEXTO);
        UIManager.put("TableHeader.background", AZUL);
        UIManager.put("TableHeader.foreground", Color.WHITE);
        UIManager.put("TableHeader.font", base.deriveFont(Font.BOLD, 13f));
        UIManager.put("Table.rowHeight", 28);
        for (String tipo : new String[]{"TextField", "PasswordField", "FormattedTextField", "TextArea"}) {
            UIManager.put(tipo + ".background", new Color(0xF7FAFF));
            UIManager.put(tipo + ".foreground", TEXTO);
            UIManager.put(tipo + ".caretForeground", ACCION);
            UIManager.put(tipo + ".selectionBackground", SELECCION);
            UIManager.put(tipo + ".selectionForeground", TEXTO);
        }
    }

    static void aviso(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Aviso", JOptionPane.WARNING_MESSAGE);
    }

    static void info(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "INDEA", JOptionPane.INFORMATION_MESSAGE);
    }

    static boolean confirmar(Component padre, String mensaje) {
        return JOptionPane.showConfirmDialog(padre, mensaje, "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    static void manejar(Component padre, RuntimeException e) {
        if (e instanceof ValidacionException || e instanceof ReglaNegocioException || e instanceof AccesoDenegadoException) {
            aviso(padre, e.getMessage());
        } else {
            JOptionPane.showMessageDialog(padre, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    static JPanel encabezado(String titulo, String subtitulo) {
        JPanel p = new JPanel(new java.awt.BorderLayout()) {
            @Override protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                java.awt.Graphics2D d = (java.awt.Graphics2D)g.create();
                int w = getWidth(), h = getHeight();
                d.setPaint(new java.awt.GradientPaint(0, 0, AZUL, w, h, ACCION));
                d.fillRoundRect(0, 0, w, h, 14, 14);
                d.setColor(new Color(255, 255, 255, 16));
                for (int x = w - 230; x < w; x += 78) {
                    d.fillPolygon(new int[]{x, x+26, x+h+26, x+h}, new int[]{h,h,0,0}, 4);
                }
                d.dispose();
            }
        };
        p.setOpaque(false);
        p.setBackground(AZUL);
        p.setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));
        JLabel t = new JLabel(titulo);
        t.setForeground(Color.WHITE);
        t.setFont(t.getFont().deriveFont(Font.BOLD, 25f));
        JPanel texto = new JPanel(new java.awt.BorderLayout());
        texto.setOpaque(false);
        texto.add(t, java.awt.BorderLayout.NORTH);
        p.add(texto, java.awt.BorderLayout.CENTER);
        JLabel marca = new JLabel(MARCA);
        marca.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));
        marca.getAccessibleContext().setAccessibleName("Logo de INDEA");
        p.add(marca, java.awt.BorderLayout.EAST);
        if (subtitulo != null) {
            JLabel s = new JLabel(subtitulo);
            s.setForeground(new Color(0xE1EBFC));
            texto.add(s, java.awt.BorderLayout.SOUTH);
        }
        return p;
    }

    static void fila(JPanel panel, int y, String etiqueta, JComponent campo) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = y;
        g.anchor = GridBagConstraints.NORTHWEST;
        g.insets = new Insets(4, 4, 4, 8);
        panel.add(new JLabel(etiqueta, SwingConstants.LEFT), g);
        g = new GridBagConstraints();
        g.gridx = 1;
        g.gridy = y;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 0, 4, 4);
        panel.add(campo, g);
    }

    // Los formularios y detalles conservan su altura minima en ventanas pequenas.
    // La rueda del raton mueve el contenido sin ocultar campos o tablas.
    static JScrollPane desplazamientoVertical(JPanel contenido) {
        JPanel contenedor = new PanelDesplazable();
        contenedor.setLayout(new BorderLayout());
        contenedor.add(contenido, BorderLayout.NORTH);
        JScrollPane desplazamiento = new JScrollPane(contenedor,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        desplazamiento.setBorder(null);
        desplazamiento.getViewport().setBackground(FONDO);
        ajustarDesplazamiento(desplazamiento);
        return desplazamiento;
    }

    static void ajustarDesplazamiento(JScrollPane desplazamiento) {
        desplazamiento.getVerticalScrollBar().setUnitIncrement(24);
        desplazamiento.getHorizontalScrollBar().setUnitIncrement(30);
    }

    private static final class PanelDesplazable extends JPanel implements Scrollable {
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle visible, int orientacion, int direccion) { return 24; }
        @Override public int getScrollableBlockIncrement(Rectangle visible, int orientacion, int direccion) {
            return Math.max(24, visible.height - 48);
        }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    static JPanel formulario() {
        return new JPanel(new GridBagLayout());
    }

    static DefaultTableModel modeloTabla(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    static String dinero(java.math.BigDecimal monto) {
        return String.format("$%,.2f", monto == null ? java.math.BigDecimal.ZERO : monto);
    }
}
