package com.indea.vista;

import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public final class FoliosSmoke {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var t=new TablaFolios(new DefaultTableModel(
                    new Object[][]{{"OT-01","Ana","Laptop"},{"OT-02","Luis","Impresora"}},
                    new String[]{"Folio","Cliente","Equipo"}));
            t.changeSelection(1,1,false,false);
            check(t.getSelectedRow()==-1);
            t.seleccionarFolio(1);
            check(t.isCellSelected(1,0));
            check(!t.isCellSelected(1,1));
            check(!t.isCellSelected(1,2));
            t.changeSelection(0,2,false,false);
            check(t.isCellSelected(1,0));
            t.seleccionarFolio(0);
            check(t.isCellSelected(0,0) && !t.isCellSelected(1,0));
            t.clearSelection();
            check(t.getSelectedRow()==-1);
        });
        System.out.println("OK: selección exclusiva de celdas de folio");
    }
    private static void check(boolean cumple) {
        if (!cumple) throw new AssertionError("Selección incorrecta");
    }
}
