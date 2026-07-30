package com.tanner.devconfig.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.tanner.abs.AbstractAnAction;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * 清除客户端缓存
 */
public class ClearClientCacheAction extends AbstractAnAction {

    @Override
    public void doAction(AnActionEvent event) {
        String cacheDirPath = System.getProperty("user.home") + File.separator + "NCCACHE";
        File cacheDir = new File(cacheDirPath);
        Project project = event.getProject();
        Task.Backgroundable task = new Task.Backgroundable(project, "正在清除 NC 客户端缓存…",
                false) {
            private IOException failure;

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                if (!cacheDir.exists()) {
                    return;
                }
                try {
                    FileUtils.deleteDirectory(cacheDir);
                } catch (IOException exception) {
                    failure = exception;
                }
            }

            @Override
            public void onSuccess() {
                if (failure != null) {
                    Messages.showErrorDialog("删除文件异常：\n" + failure.getMessage(), "错误");
                } else {
                    Messages.showInfoMessage("客户端缓存已清除", "完成");
                }
            }
        };
        ProgressManager.getInstance().run(task);
    }
}
