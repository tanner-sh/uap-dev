package com.tanner.debug.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import com.tanner.base.ModuleRootUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class AppGroupAction extends DefaultActionGroup {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        Module module = e.getData(LangDataKeys.MODULE);
        VirtualFile moduleRoot = module == null ? null
                : ModuleRootUtil.findPrimaryContentRoot(module);
        boolean flag = module != null
                && file != null
                && file.getParent() != null
                && module.getName().equals(file.getName())
                && moduleRoot != null
                && new File(moduleRoot.getPath() + File.separator + "META-INF"
                + File.separator + "module.xml").exists();
        e.getPresentation().setEnabledAndVisible(flag);
    }
}
