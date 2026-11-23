package com.indea.vista;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Prueba visual básica sin modificar datos ni conectarse a MySQL. */
public final class InterfazFolioSmoke {
    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        UiSetup.configure();
        SwingUtilities.invokeAndWait(() -> {
            ConsultaFolioPanel panel = new ConsultaFolioPanel();
            comprobar(tieneEjemplo(panel), "El panel interno muestra un folio incorrecto");
            ConsultaFolioDialog dialogo = new ConsultaFolioDialog((Window) null);
            comprobar(tieneEjemplo(dialogo), "La consulta pública muestra un folio incorrecto");
            JButton boton = new JButton("Cancelar");
            comprobar(boton.getUI() instanceof BotonNativoUI,
                    "Las ventanas de confirmación no aplican el estilo global de los botones");
            comprobar(boton.isFocusPainted(), "El foco de teclado debe seguir disponible");
            dialogo.dispose();
            System.out.println("OK: folio INDEA-OT y botones de confirmación sin marco interior");
        });
    }

    private static boolean tieneEjemplo(Component componente) {
        if (componente instanceof JLabel) {
            String texto = ((JLabel) componente).getText();
            if (texto != null && texto.contains("ejemplo: INDEA-OT-4016")) return true;
        }
        if (componente instanceof Container) {
            for (Component hijo : ((Container) componente).getComponents()) {
                if (tieneEjemplo(hijo)) return true;
            }
        }
        return false;
    }

    private static void comprobar(boolean ok, String mensaje) {
        if (!ok) throw new AssertionError(mensaje);
    }
}
