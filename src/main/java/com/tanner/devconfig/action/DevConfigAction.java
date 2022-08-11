package com.tanner.devconfig.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.tanner.abs.AbstractAnAction;
import com.tanner.devconfig.DevConfigDialog;

/**
 * 集成环境配置按钮
 */
public class DevConfigAction extends AbstractAnAction {

  @Override
  public void doAction(AnActionEvent event) {
    DevConfigDialog dialog = new DevConfigDialog();
    dialog.setVisible(true);
  }
}
