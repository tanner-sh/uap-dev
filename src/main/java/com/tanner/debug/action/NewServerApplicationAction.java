package com.tanner.debug.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.tanner.abs.AbstractAnAction;
import com.tanner.debug.util.CreatApplicationConfigurationUtil;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public class NewServerApplicationAction extends AbstractAnAction {

    @Override
    public void doAction(@NotNull AnActionEvent event) {
        String message = "Success";
        try {
            CreatApplicationConfigurationUtil.createApplicationConfiguration(event, true);
            Messages.showInfoMessage(message, "Tips");
        } catch (Exception e) {
            message = e.getMessage();
            Messages.showInfoMessage(message, "Error");
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        boolean flag = isModuleChild(file, e);
        e.getPresentation().setEnabledAndVisible(flag);
    }

}
