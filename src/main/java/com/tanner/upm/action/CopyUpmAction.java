package com.tanner.upm.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.tanner.abs.AbstractAnAction;
import com.tanner.base.ModuleRootUtil;
import com.tanner.base.UapProjectEnvironment;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * 复制upm、res文件到nchome
 */
public class CopyUpmAction extends AbstractAnAction {

    @Override
    public void doAction(AnActionEvent event) {
        EjbConfCopyUtil util = new EjbConfCopyUtil();
        Project project = event.getProject();
        Module module = getSelectModule(event);
        VirtualFile selected = getSelectFile(event);
        UapProjectEnvironment environment = UapProjectEnvironment.getInstance(project);
        if (environment == null) {
            return;
        }
        String homePath = environment.getUapHomePath();
        String ncModuleName = module == null ? null : util.getNCModuleName(module);
        String sourcePath = selected == null ? null : selected.getPath();
        Task.Backgroundable task = new Task.Backgroundable(project,
                "Copying UPM/REST files...", true) {
            private int copied;
            private Exception failure;

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    copied = util.copy(homePath, ncModuleName, sourcePath, indicator);
                } catch (ProcessCanceledException exception) {
                    throw exception;
                } catch (Exception exception) {
                    failure = exception;
                }
            }

            @Override
            public void onSuccess() {
                if (failure != null) {
                    Messages.showErrorDialog(failure.getMessage(), "Error");
                } else if (copied == 0) {
                    Messages.showWarningDialog("未找到可复制的 UPM/REST 文件", "Tips");
                } else {
                    Messages.showInfoMessage("Copied " + copied + " file(s)", "Tips");
                }
            }
        };
        ProgressManager.getInstance().run(task);
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
                    VirtualFile moduleRoot = module == null ? null
                            : ModuleRootUtil.findPrimaryContentRoot(module);
                    if (moduleRoot != null) {
                        if (selectFile.getParent().equals(moduleRoot)) {
                            flag = new File(selectFile.getPath() + File.separator + "component.xml").exists();
                        }
                    }
                }
            }
        }
        e.getPresentation().setEnabledAndVisible(flag);
    }
}
