package com.indea.vista;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableModel;

/** En Seguimiento solo la columna de folios recibe selección. */
final class TablaFolios extends JTable {
    TablaFolios(TableModel datos) {
        super(datos);
        setCellSelectionEnabled(true);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        getColumnModel().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        getTableHeader().setReorderingAllowed(false);
    }

    @Override public void changeSelection(int fila, int columna, boolean alternar, boolean extender) {
        if (columna==0) super.changeSelection(fila,0,false,false);
    }

    void seleccionarFolio(int fila) { changeSelection(fila,0,false,false); }
}
