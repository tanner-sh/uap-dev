package com.tanner.ui;

import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BulkTableModelTest {

    @Test
    public void replacesRowsAndBulkSelectsWithSingleEvents() {
        BulkTableModel model = new BulkTableModel(
                new String[]{"序号", "选中", "名称"},
                new Class<?>[]{Integer.class, Boolean.class, String.class},
                Set.of(1));
        AtomicInteger events = new AtomicInteger();
        model.addTableModelListener(event -> events.incrementAndGet());

        model.replaceRows(List.of(
                new Object[]{1, false, "a"},
                new Object[]{2, false, "b"}));

        assertEquals(1, events.get());
        assertEquals(2, model.getRowCount());
        assertTrue(model.isCellEditable(0, 1));
        assertFalse(model.isCellEditable(0, 2));

        model.setBooleanColumn(1, true);

        assertEquals(2, events.get());
        assertEquals(Boolean.TRUE, model.getValueAt(0, 1));
        assertEquals(Boolean.TRUE, model.getValueAt(1, 1));
    }
}
