package com.tanner.datadictionary.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.tanner.abs.AbstractAnAction;

public class ExportDataDictionaryAction extends AbstractAnAction {

    @Override
    public void doAction(AnActionEvent event) {
        DataDictionaryExportDlg dialog = new DataDictionaryExportDlg(event);
        dialog.show();
    }

}
