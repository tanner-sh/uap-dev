package com.tanner.upm.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.tanner.abs.AbstractAnAction;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * 复制upm、res文件到nchome
 */
public class CopyUpmAction extends AbstractAnAction {

    @Override
    public void doAction(AnActionEvent event) {
        EjbConfCopyUtil util = new EjbConfCopyUtil();
        try {
            int copied = util.copy(event);
            if (copied == 0) {
                Messages.showWarningDialog("未找到可复制的 UPM/REST 文件", "Tips");
            } else {
                Messages.showInfoMessage("Copied " + copied + " file(s)", "Tips");
            }
        } catch (Exception exception) {
            Messages.showErrorDialog(exception.getMessage(), "Error");
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        VirtualFile selectFile = getSelectFile(e);
        boolean flag;
        if (selectFile == null || selectFile.getParent() == null) {
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
