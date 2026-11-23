package com.indea.vista;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Rectangle;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalButtonUI;

/**
 * Mantiene el aspecto nativo de los botones en los cuadros de diálogo,
 * sustituyendo el rectángulo de foco alrededor del texto por un borde exterior.
 * El foco de teclado continúa siendo visible.
 */
public final class BotonNativoUI extends MetalButtonUI {
    public static ComponentUI createUI(JComponent componente) {
        return new BotonNativoUI();
    }

    @Override
    protected void paintFocus(Graphics g, AbstractButton boton,
                              Rectangle vista, Rectangle texto, Rectangle icono) {
        if (!boton.isFocusPainted()) return;
        int ancho = boton.getWidth(), alto = boton.getHeight();
        if (ancho < 8 || alto < 8) return;
        Graphics2D d = (Graphics2D) g.create();
        try {
            d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            d.setColor(Ui.ACCION);
            d.setStroke(new BasicStroke(1.4f));
            d.drawRoundRect(2, 2, ancho - 5, alto - 5, 8, 8);
        } finally {
            d.dispose();
        }
    }
}
