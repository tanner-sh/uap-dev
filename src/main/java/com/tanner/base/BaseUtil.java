package com.tanner.base;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class BaseUtil {

  private BaseUtil() {

  }

  @NotNull
  public static Project getProject(@NotNull AnActionEvent event) {
    {
      Project project = event.getProject();
      if (project == null) {
        Messages.showMessageDialog("无法获取当前项目", "Error", Messages.getErrorIcon());
        throw new RuntimeException("无法获取当前项目");
      }
      return project;
    }
  }

  @NotNull
  public static Module getModule(@NotNull AnActionEvent event) {
    {
      Module module = event.getData(LangDataKeys.MODULE);
      if (module == null) {
        Messages.showMessageDialog("无法获取当前模块", "Error", Messages.getErrorIcon());
        throw new RuntimeException("无法获取当前模块");
      }
      return module;
    }
  }
}
