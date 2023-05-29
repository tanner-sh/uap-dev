package com.tanner.devconfig.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.tanner.abs.AbstractAnAction;
import com.tanner.script.export.dlg.ScriptExportDlg;

/**
 * 导出脚本
 */
public class ScriptExportAction extends AbstractAnAction {

    @Override
    public void doAction(AnActionEvent event) {
        ScriptExportDlg dialog = new ScriptExportDlg();
        dialog.setVisible(true);
    }
}
