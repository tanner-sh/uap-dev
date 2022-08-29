package com.tanner.devconfig.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.tanner.abs.AbstractAnAction;
import com.tanner.script.export.dlg.ScriptExportDlg;

/**
 * 打开uap目录
 */
public class SciptExportAction extends AbstractAnAction {

  @Override
  public void doAction(AnActionEvent event) {
    ScriptExportDlg dialog = new ScriptExportDlg();
    dialog.setVisible(true);
  }
}
