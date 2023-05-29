package com.tanner.datadictionary.action;

import com.tanner.abs.AbstractButtonAction;
import com.tanner.abs.AbstractDataSourceDialog;
import com.tanner.abs.AbstractDialog;
import com.tanner.base.BusinessException;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class DeSelectAllAction extends AbstractButtonAction {

    public DeSelectAllAction(AbstractDialog dialog) {
        super(dialog);
    }

    @Override
    public void doAction(ActionEvent event) throws BusinessException {
        AbstractDataSourceDialog dlg = (AbstractDataSourceDialog) getDialog();
        JTable dbTable = getDialog().getComponent(JTable.class, "dbTable");
        int rowCount = dbTable.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            dbTable.getModel().setValueAt(false, i, 1);
        }
    }

}
