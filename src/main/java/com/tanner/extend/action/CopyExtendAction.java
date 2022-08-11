package com.tanner.extend.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.impl.FsRoot;
import com.tanner.abs.AbstractAnAction;
import java.io.File;
import org.jetbrains.annotations.NotNull;

public class CopyExtendAction extends AbstractAnAction {

  @Override
  public void doAction(AnActionEvent event) {
    try {
      ExtendCopyUtil.copyToNCHome(event);
      Messages.showInfoMessage("success", "Tips");
    } catch (Exception e) {
      Messages.showInfoMessage(e.getMessage(), "Error");
    }
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    VirtualFile selectFile = getSelectFile(e);
    boolean flag;
    if (selectFile == null || selectFile instanceof FsRoot) {
      flag = false;
    } else {
      File file = new File(selectFile.getPath());
      if (file.isFile()) {
        flag = file.getName().endsWith(".xml") && file.getPath().contains("yyconfig/modules") && (
            file.getParent().endsWith("action") || file.getParent().endsWith("authorize"));
      } else {
        flag = isModuleChild(selectFile, e);
        if (flag) {
          Module module = getSelectModule(e);
          if (module != null && module.getModuleFile() != null) {
            if (selectFile.getParent().equals(module.getModuleFile().getParent())) {
              flag = new File(selectFile.getPath() + File.separator + "component.xml").exists();
            }
          }
        }
      }
    }
    e.getPresentation().setEnabledAndVisible(flag);
  }
}
