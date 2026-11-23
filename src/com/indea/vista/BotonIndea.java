package com.indea.vista;

import javax.swing.*;
import java.awt.*;

/** Botón común para acciones y navegación. Conserva los atajos y eventos de JButton. */
final class BotonIndea extends JButton {
    private boolean menu;
    private boolean activo;
    private final boolean principal;

    BotonIndea(String texto) { this(texto, false); }

    BotonIndea(String texto, boolean principal) {
        super(texto);
        this.principal = principal;
        setFont(getFont().deriveFont(Font.BOLD, 14f));
        setForeground(principal ? Color.WHITE : Color.BLACK);
        setOpaque(false);
        setContentAreaFilled(false);
        setBorder(BorderFactory.createEmptyBorder(7, 12, 7, 12));
        setFocusPainted(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    void comoMenu() {
        menu = true;
        setHorizontalAlignment(LEFT);
        setForeground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 10));
    }

    void activar(boolean valor) { activo = valor; repaint(); }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D d = (Graphics2D)g.create();
        d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        boolean sobre = getModel().isRollover() || getModel().isArmed();
        Color fondo;
        if (menu) fondo = activo ? new Color(0x244F91) : (sobre ? new Color(0x203C65) : Ui.AZUL);
        else if (!isEnabled()) fondo = new Color(0xE8EDF5);
        else if (principal) fondo = sobre ? new Color(0x1E4C9D) : Ui.ACCION;
        else fondo = sobre ? new Color(0xE4EEFB) : Color.WHITE;
        d.setColor(fondo);
        d.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
        if (!menu || hasFocus()) {
            d.setColor(hasFocus() ? (menu ? Color.WHITE : Ui.ACCION) : (principal ? Ui.ACCION : Ui.LINEA));
            d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
        }
        if (menu && activo) {
            d.setColor(new Color(0xF2B34F));
            d.fillRoundRect(3, 7, 4, getHeight()-14, 4, 4);
        }
        d.dispose();
        super.paintComponent(g);
    }
}
