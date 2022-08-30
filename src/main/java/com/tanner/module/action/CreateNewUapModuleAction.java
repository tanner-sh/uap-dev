package com.tanner.module.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.tanner.abs.AbstractAnAction;
import com.tanner.module.NewModuleDialog;

/**
 * 新建uap模块
 */
public class CreateNewUapModuleAction extends AbstractAnAction {

  @Override
  public void doAction(AnActionEvent event) {
    NewModuleDialog dialog = new NewModuleDialog(event);
    dialog.setSize(900, 300);
    dialog.setLocationRelativeTo(null);
    dialog.setVisible(true);
    dialog.requestFocus();
  }
}
