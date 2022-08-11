package com.tanner.abs;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.tanner.base.ProjectManager;
import java.io.File;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * idea按钮抽象类
 */
public abstract class AbstractAnAction extends AnAction {

  @Override
  public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
    ProjectManager.getInstance().setProject(anActionEvent.getProject());
    doAction(anActionEvent);
  }

  /**
   * 子类实现
   *
   * @param event
   */
  public abstract void doAction(AnActionEvent event);

  /**
   * 获取选中文件
   *
   * @param event
   * @return
   */
  public VirtualFile getSelectFile(AnActionEvent event) {
    return event.getData(CommonDataKeys.VIRTUAL_FILE);
  }

  public VirtualFile[] getSelectFileArr(AnActionEvent event) {
    return event.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
  }

  /**
   * 是否nc module 根目录
   *
   * @param event
   * @return
   */
  public boolean isUapMoudle(@NotNull AnActionEvent event) {
    VirtualFile selectFile = getSelectFile(event);
    if (selectFile == null) {
      return false;
    }
    Module module = ModuleUtil.findModuleForFile(selectFile,
        Objects.requireNonNull(event.getProject()));
    if (module == null) {
      return false;
    }
    return module.getName().equals(selectFile.getName()) && new File(
        selectFile.getPath() + File.separator + "META-INF" + File.separator
            + "module.xml").exists();
  }

  public boolean isModuleChild(VirtualFile file, AnActionEvent event) {
    boolean flag;
    if (file == null) {
      return false;
    }
    Module module = ModuleUtil.findModuleForFile(file, Objects.requireNonNull(event.getProject()));
    if (module == null) {
      return false;
    }
    VirtualFile moduleFile = module.getModuleFile();
    if (moduleFile == null) {
      return false;
    }
    flag = new File(moduleFile.getParent().getPath() + File.separator + "META-INF" + File.separator
        + "module.xml").exists();
    return flag;
  }

  public boolean isMavenModuleChild(VirtualFile file, AnActionEvent event) {
    boolean flag;
    if (file == null) {
      return false;
    }
    Module module = ModuleUtil.findModuleForFile(file, Objects.requireNonNull(event.getProject()));
    if (module == null) {
      return false;
    }
    VirtualFile moduleFile = module.getModuleFile();
    if (moduleFile == null) {
      return false;
    }
    flag = new File(moduleFile.getParent().getPath() + File.separator + "pom.xml").exists();
    return flag;
  }

  /**
   * 获取选中模块
   *
   * @param event
   * @return
   */
  public Module getSelectModule(AnActionEvent event) {
    return event.getData(LangDataKeys.MODULE);
  }
}
