package com.tanner.library.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.tanner.abs.AbstractAnAction;
import com.tanner.base.BusinessException;
import com.tanner.base.ProjectManager;
import org.jetbrains.annotations.NotNull;

/**
 * 设置模块累路径
 */
public class LibrariesSetAction extends AbstractAnAction {

  @Override
  public void doAction(AnActionEvent event) {
    String message = "success";
    try {
      boolean flag = isUapMoudle(event);
      if (flag) {
        ProjectManager.getInstance()
            .setModuleLibrary(event.getProject(), event.getData(LangDataKeys.MODULE));
        Messages.showInfoMessage(message, "Tips");
      }
    } catch (BusinessException e) {
      message = e.getMessage();
      Messages.showInfoMessage(message, "Error");
    }
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    VirtualFile file = getSelectFile(e);
    Module module = getSelectModule(e);
    boolean flag = isModuleChild(file, e);
    e.getPresentation().setEnabledAndVisible(flag);
  }
}
