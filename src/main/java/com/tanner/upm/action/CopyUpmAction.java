package com.tanner.upm.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.impl.FsRoot;
import com.tanner.abs.AbstractAnAction;
import java.io.File;
import org.jetbrains.annotations.NotNull;

/**
 * 复制upm、res文件到nchome
 */
public class CopyUpmAction extends AbstractAnAction {

  @Override
  public void doAction(AnActionEvent event) {
    String message = "success";
    EjbConfCopyUtil util = new EjbConfCopyUtil();
    util.copy(event);
    Messages.showInfoMessage(message, "Tips");
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
        flag = file.getPath().contains("META-INF") && (file.getName().endsWith(".upm")
            || file.getName().endsWith(".rest"));
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
