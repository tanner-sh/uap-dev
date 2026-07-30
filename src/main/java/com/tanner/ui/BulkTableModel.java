package com.tanner.ui;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A table model that replaces or updates rows with a single Swing model event.
 */
public class BulkTableModel extends AbstractTableModel {

    private final String[] columnNames;
    private final Class<?>[] columnClasses;
    private final Set<Integer> editableColumns;
    private final List<Object[]> rows = new ArrayList<>();

    public BulkTableModel(String[] columnNames, Class<?>[] columnClasses,
                          Set<Integer> editableColumns) {
        this.columnNames = columnNames.clone();
        this.columnClasses = columnClasses.clone();
        this.editableColumns = Set.copyOf(editableColumns);
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnClasses[columnIndex];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return rows.get(rowIndex)[columnIndex];
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        rows.get(row)[column] = value;
        fireTableCellUpdated(row, column);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return editableColumns.contains(column);
    }

    public void replaceRows(List<Object[]> rows) {
        this.rows.clear();
        for (Object[] row : rows) {
            this.rows.add(row.clone());
        }
        fireTableDataChanged();
    }

    public void clearRows() {
        if (rows.isEmpty()) {
            return;
        }
        rows.clear();
        fireTableDataChanged();
    }

    public void setBooleanColumn(int column, boolean selected) {
        if (rows.isEmpty()) {
            return;
        }
        for (Object[] row : rows) {
            row[column] = selected;
        }
        fireTableRowsUpdated(0, getRowCount() - 1);
    }
}
