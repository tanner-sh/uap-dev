package com.tanner.patcher.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.vfs.VirtualFile;
import com.tanner.abs.AbstractAnAction;
import org.jetbrains.annotations.NotNull;


public class ExportPatcherAction extends AbstractAnAction {

    @Override
    public void doAction(AnActionEvent event) {
        PatcherDialog dialog = new PatcherDialog(event);
        dialog.setSize(900, 600);
        dialog.show();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        VirtualFile[] selectFileArr = getSelectFileArr(e);
        boolean flag = true;
        if (selectFileArr == null || selectFileArr.length == 0) {
            flag = false;
        } else {
            for (VirtualFile virtualFile : selectFileArr) {
                if (virtualFile.getParent() == null
                        || !(isModuleChild(virtualFile, e)
                        || isMavenModuleChild(virtualFile, e))) {
                    flag = false;
                    break;
                }
            }
        }
        e.getPresentation().setEnabledAndVisible(flag);
    }
}
