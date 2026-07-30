package com.tanner.ui;

import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.Set;
import java.util.Vector;

/**
 * A table model that replaces or updates rows with a single Swing model event.
 */
public class BulkTableModel extends DefaultTableModel {

    private final Class<?>[] columnClasses;
    private final Set<Integer> editableColumns;

    public BulkTableModel(String[] columnNames, Class<?>[] columnClasses,
                          Set<Integer> editableColumns) {
        super(columnNames, 0);
        this.columnClasses = columnClasses.clone();
        this.editableColumns = Set.copyOf(editableColumns);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnClasses[columnIndex];
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return editableColumns.contains(column);
    }

    public void replaceRows(List<Object[]> rows) {
        dataVector.clear();
        for (Object[] row : rows) {
            Vector<Object> values = new Vector<>(row.length);
            for (Object value : row) {
                values.add(value);
            }
            dataVector.add(values);
        }
        fireTableDataChanged();
    }

    public void clearRows() {
        if (dataVector.isEmpty()) {
            return;
        }
        dataVector.clear();
        fireTableDataChanged();
    }

    public void setBooleanColumn(int column, boolean selected) {
        if (dataVector.isEmpty()) {
            return;
        }
        for (Vector<?> row : dataVector) {
            @SuppressWarnings("unchecked")
            Vector<Object> values = (Vector<Object>) row;
            values.set(column, selected);
        }
        fireTableRowsUpdated(0, getRowCount() - 1);
    }
}
