package com.tanner.logwatcher;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.tanner.base.UapProjectEnvironment;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;

public class LogWatcherToolWindowFactory implements ToolWindowFactory, DumbAware {

    @Override
    public void createToolWindowContent(@NotNull Project project, ToolWindow toolWindow) {
        // 创建 ConsoleView
        ConsoleView consoleView = TextConsoleBuilderFactory.getInstance()
                .createBuilder(project).getConsole();
        // 将 ConsoleView 添加到工具窗口
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(consoleView.getComponent(), "Logs", false);
        toolWindow.getContentManager().addContent(content);
        // 启动日志监控服务
        LogWatcherService logWatcherService = new LogWatcherService();
        logWatcherService.setConsoleView(consoleView);
        Disposer.register(content, consoleView);
        Disposer.register(content, logWatcherService);
        // 获取日志文件夹位置
        UapProjectEnvironment uapProjectEnvironment = UapProjectEnvironment.getInstance(project);
        if (uapProjectEnvironment == null) {
            logWatcherService.appendLog("Please open a project");
            return;
        }
        String uapHomePath = uapProjectEnvironment.getUapHomePath();
        if (StringUtils.isBlank(uapHomePath)) {
            logWatcherService.appendLog("Please set uapHomePath");
            return;
        }
        Path logDirPath = Paths.get(uapHomePath, "nclogs");
        ApplicationManager.getApplication().executeOnPooledThread(() -> logWatcherService.startWatching(logDirPath));
    }

}
