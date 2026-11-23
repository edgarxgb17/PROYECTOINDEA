package com.indea.vista;

import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.table.*;

/** Presentación común de los listados, compatible con la selección por celda de folios. */
class TablaIndea extends JTable {
    private static final Color ALTERNA = new Color(0xF2F6FC);

    TablaIndea() { this(new DefaultTableModel()); }

    TablaIndea(TableModel datos) {
        super(datos);
        setRowHeight(34);
        setShowVerticalLines(false);
        setIntercellSpacing(new Dimension(0, 1));
        setFillsViewportHeight(true);
        getTableHeader().setPreferredSize(new Dimension(0, 36));
        getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object valor,
                    boolean seleccionado, boolean foco, int fila, int columna) {
                super.getTableCellRendererComponent(t, valor, seleccionado, foco, fila, columna);
                setBackground(Ui.AZUL);
                setForeground(Color.WHITE);
                setFont(t.getTableHeader().getFont());
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 5));
                return this;
            }
        });
    }

    @Override public Component prepareRenderer(TableCellRenderer renderer, int fila, int columna) {
        Component c = super.prepareRenderer(renderer, fila, columna);
        c.setBackground(isCellSelected(fila, columna) ? Ui.SELECCION : (fila % 2 == 0 ? Color.WHITE : ALTERNA));
        c.setForeground(Color.BLACK);
        if (c instanceof JLabel celda) celda.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 5));
        return c;
    }

    @Override public String getToolTipText(MouseEvent e) {
        int fila = rowAtPoint(e.getPoint()), columna = columnAtPoint(e.getPoint());
        if (fila < 0 || columna < 0) return null;
        return java.util.Objects.toString(getValueAt(fila, columna), null);
    }
}
