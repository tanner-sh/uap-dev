package com.tanner.logwatcher;

import com.intellij.icons.AllIcons;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import com.tanner.base.UapProjectEnvironment;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LogWatcherToolWindowFactory implements ToolWindowFactory, DumbAware {

    @Override
    public void createToolWindowContent(@NotNull Project project, ToolWindow toolWindow) {
        ConsoleView consoleView = TextConsoleBuilderFactory.getInstance()
                .createBuilder(project).getConsole();
        LogWatcherService logWatcherService = new LogWatcherService();
        logWatcherService.setConsoleView(consoleView);

        JPanel rootPanel = new JPanel(new BorderLayout());
        JBLabel statusLabel = new JBLabel("等待配置日志目录");
        statusLabel.setBorder(JBUI.Borders.emptyRight(8));
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(new AnAction("刷新监控", "重新扫描日志目录", AllIcons.Actions.Refresh) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent event) {
                startWatcher(project, logWatcherService, statusLabel);
            }
        });
        actionGroup.add(new AnAction("清空输出", "清空日志窗口", AllIcons.Actions.GC) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent event) {
                consoleView.clear();
            }
        });
        JComponent toolbar = ActionManager.getInstance()
                .createActionToolbar(ActionPlaces.TOOLWINDOW_TITLE, actionGroup, true)
                .getComponent();
        JPanel header = new JPanel(new BorderLayout());
        header.add(toolbar, BorderLayout.WEST);
        header.add(statusLabel, BorderLayout.EAST);
        rootPanel.add(header, BorderLayout.NORTH);
        rootPanel.add(consoleView.getComponent(), BorderLayout.CENTER);

        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(rootPanel, "日志", false);
        toolWindow.getContentManager().addContent(content);
        Disposer.register(content, consoleView);
        Disposer.register(content, logWatcherService);
        Timer statusTimer = new Timer(1000, event -> {
            String text = statusLabel.getToolTipText();
            if (text != null) {
                statusLabel.setText("监控文件：" + logWatcherService.watchedFileCount());
            }
        });
        statusTimer.start();
        Disposer.register(content, statusTimer::stop);
        startWatcher(project, logWatcherService, statusLabel);
    }

    private static void startWatcher(Project project, LogWatcherService logWatcherService,
                                     JBLabel statusLabel) {
        logWatcherService.stopWatching();
        UapProjectEnvironment uapProjectEnvironment = UapProjectEnvironment.getInstance(project);
        if (uapProjectEnvironment == null) {
            statusLabel.setText("请先打开项目");
            statusLabel.setToolTipText(null);
            logWatcherService.appendLog("请先打开项目");
            return;
        }
        String uapHomePath = uapProjectEnvironment.getUapHomePath();
        if (StringUtils.isBlank(uapHomePath)) {
            statusLabel.setText("请先配置 NC Home");
            statusLabel.setToolTipText(null);
            logWatcherService.appendLog("请先配置 NC Home");
            return;
        }
        Path logDirPath = Paths.get(uapHomePath, "nclogs");
        statusLabel.setText("正在启动监控…");
        statusLabel.setToolTipText(logDirPath.toString());
        ApplicationManager.getApplication().executeOnPooledThread(
                () -> logWatcherService.startWatching(logDirPath));
    }

}
