package com.tanner.datadictionary.action;

import com.tanner.abs.AbstractButtonAction;
import com.tanner.abs.AbstractDialog;
import com.tanner.base.BusinessException;
import com.tanner.ui.BulkTableModel;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class SelectAllAction extends AbstractButtonAction {

    public SelectAllAction(AbstractDialog dialog) {
        super(dialog);
    }

    @Override
    public void doAction(ActionEvent event) throws BusinessException {
        JTable dbTable = ((DataDictionaryExportDlg) getDialog()).table();
        if (dbTable.getModel() instanceof BulkTableModel model) {
            model.setBooleanColumn(1, true);
            return;
        }
        int rowCount = dbTable.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            dbTable.getModel().setValueAt(true, i, 1);
        }
    }

}
