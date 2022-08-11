package com.tanner.module.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.tanner.abs.AbstractAnAction;
import com.tanner.module.NewComponentDialog;
import java.io.File;
import org.jetbrains.annotations.NotNull;

/**
 * 新建nc组件
 */
public class CreateNCComponentAction extends AbstractAnAction {

  @Override
  public void doAction(AnActionEvent event) {
    //判断选中的是不是nc module
    boolean isNcModule = isUapMoudle(event);
    if (!isNcModule) {
      Messages.showErrorDialog("Please select nc module root!", "Error");
      return;
    }
    NewComponentDialog dialog = new NewComponentDialog(event);
    dialog.setSize(900, 300);
    dialog.setLocationRelativeTo(null);
    dialog.setVisible(true);
    dialog.requestFocus();
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    VirtualFile file = getSelectFile(e);
    Module module = getSelectModule(e);
    boolean flag =
        file != null && module != null && module.getName().equals(file.getName()) && new File(
            file.getPath() + File.separator + "META-INF" + File.separator + "module.xml").exists();
    e.getPresentation().setEnabledAndVisible(flag);
  }
}
