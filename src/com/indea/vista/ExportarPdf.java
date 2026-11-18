package com.indea.vista;

import com.indea.util.ReportePdf;
import java.awt.Component;
import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.function.Supplier;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

final class ExportarPdf {
    private ExportarPdf() {}

    static void guardar(Component origen,String archivo,Supplier<ReportePdf.Informe> crear) {
        JFileChooser selector=new JFileChooser();
        selector.setDialogTitle("Guardar reporte PDF");
        selector.setFileFilter(new FileNameExtensionFilter("Documento PDF (*.pdf)","pdf"));
        selector.setAcceptAllFileFilterUsed(false);
        // Una ruta relativa permite que el selector conserve su carpeta habitual.
        selector.setSelectedFile(new java.io.File(archivo+".pdf"));
        if (selector.showSaveDialog(origen)!=JFileChooser.APPROVE_OPTION) return;
        Path destino=selector.getSelectedFile().toPath().toAbsolutePath();
        if (!destino.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            destino=destino.resolveSibling(destino.getFileName()+".pdf");
        }
        if (Files.exists(destino) && !Ui.confirmar(origen,"El archivo ya existe. ¿Quieres reemplazarlo?")) return;
        try {
            ReportePdf.guardar(destino,crear.get());
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                String[] opciones={"Abrir PDF","Cerrar"};
                int elegido=JOptionPane.showOptionDialog(origen,"Reporte guardado en:\n"+destino,"PDF listo",
                        JOptionPane.DEFAULT_OPTION,JOptionPane.INFORMATION_MESSAGE,null,opciones,opciones[0]);
                if (elegido==0) {
                    try { Desktop.getDesktop().open(destino.toFile()); }
                    catch (IOException | SecurityException e) { Ui.aviso(origen,"El PDF se guardó. Ábrelo desde "+destino+"."); }
                }
            } else Ui.info(origen,"Reporte guardado en:\n"+destino);
        } catch (IOException e) {
            Ui.aviso(origen,"No se pudo guardar el PDF: "+e.getMessage());
        } catch (RuntimeException e) {
            Ui.manejar(origen,e);
        }
    }
}
