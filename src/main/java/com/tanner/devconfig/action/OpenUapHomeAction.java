package com.tanner.devconfig.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.tanner.abs.AbstractAnAction;
import com.tanner.base.UapProjectEnvironment;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;

/**
 * 打开uap目录
 */
public class OpenUapHomeAction extends AbstractAnAction {

  @Override
  public void doAction(AnActionEvent event) {
    String ncHomePath = UapProjectEnvironment.getInstance().getUapHomePath();
    if (StringUtils.isBlank(ncHomePath)) {
      Messages.showErrorDialog("没有设置uap home 请检查!", "错误");
      return;
    }
    boolean exists = new File(ncHomePath).exists();
    if (!exists) {
      Messages.showErrorDialog("uap home设置错误，请检查!\n" + ncHomePath, "错误");
      return;
    }
    try {
      Desktop.getDesktop().open(new File(ncHomePath));
    } catch (IOException e) {
      Messages.showErrorDialog("打开文件夹发生错误!\n" + e.getMessage(), "错误");
    }
  }
}
