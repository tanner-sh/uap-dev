package com.tanner.module.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.impl.FsRoot;
import com.tanner.abs.AbstractAnAction;
import com.tanner.base.BusinessException;
import com.tanner.module.util.ModuleUtil;
import java.io.File;
import org.jetbrains.annotations.NotNull;

/**
 * 转化为module
 */
public class ConvertModuleAction extends AbstractAnAction {

  @Override
  public void doAction(AnActionEvent event) {
    VirtualFile[] file = getSelectFileArr(event);
    //暂时这么愚蠢的支持批量
    ModuleUtil util = new ModuleUtil();
    boolean flag = true;
    for (VirtualFile f : file) {
      try {
        util.coverToModule(event.getProject(), f.getPath());
      } catch (BusinessException businessException) {
        Messages.showErrorDialog(businessException.getMessage(), "wrong");
        flag = false;
        break;
      }
    }
    if (flag) {
      Messages.showInfoMessage("finish", "success");
    }

  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    boolean flag = true;
    VirtualFile[] selectFileArr = getSelectFileArr(e);
    if (selectFileArr == null || selectFileArr.length == 0) {
      flag = false;
    } else {
      for (VirtualFile virtualFile : selectFileArr) {
        if (virtualFile instanceof FsRoot) {
          flag = false;
          break;
        }
        Module module = com.intellij.openapi.module.ModuleUtil.findModuleForFile(virtualFile,
            e.getProject());
        //这里当前目录如果没有转module，会找到上曾module，所以永远不是空
        flag = module != null && !module.getName().equals(virtualFile.getName()) && new File(
            virtualFile.getPath() + File.separator + "META-INF" + File.separator
                + "module.xml").exists();
      }
    }
    e.getPresentation().setEnabledAndVisible(flag);
  }
}
