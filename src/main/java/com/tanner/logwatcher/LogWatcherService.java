package com.tanner.logwatcher;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class LogWatcherService implements Disposable {

    private final String[] logFileNames = {"nc-log.log"};
    private boolean isRunning = false; // 是否正在运行
    private ConsoleView consoleView; // 内置日志展示组件
    private final Charset charset = StandardCharsets.UTF_8;
    private final List<Tailer> tailers = new CopyOnWriteArrayList<>();

    public void setConsoleView(ConsoleView consoleView) {
        this.consoleView = consoleView;
    }

    public void startWatching(Path rootDirectory) {
        if (isRunning) {
            appendLog("Log watcher is already running.");
            return;
        }
        if (rootDirectory == null || !Files.isDirectory(rootDirectory)) {
            appendLog("Log directory not found: " + rootDirectory);
            return;
        }
        isRunning = true;
        try {
            Collection<File> logFiles = FileUtils.listFiles(rootDirectory.toFile(), new String[]{"log"}, true);
            logFiles.stream()
                    .filter(logFile -> ArrayUtils.contains(logFileNames, logFile.getName()))
                    .forEach(this::addLogFile);
            appendLog("Log watcher started for directory: " + rootDirectory);
        } catch (Exception e) {
            isRunning = false;
            appendLog("Failed to scan log directory: " + e.getMessage());
        }
    }

    public void addLogFile(File file) {
        Tailer tailer = new Tailer(file, charset, new TailerListenerAdapter() {
            @Override
            public void handle(String line) {
                appendLog("[" + file.getName() + "] " + line);
            }
        }, 1000, false, false, 8192);
        Thread thread = new Thread(tailer, "log-watcher-" + file.getName());
        thread.setDaemon(true);
        tailers.add(tailer);
        thread.start();
    }

    public void stopWatching() {
        if (!isRunning) {
            return;
        }
        isRunning = false;
        for (Tailer tailer : new ArrayList<>(tailers)) {
            tailer.stop();
        }
        tailers.clear();
        appendLog("Log watcher stopped.");
    }

    public void appendLog(String message) {
        if (consoleView == null) {
            return;
        }
        ApplicationManager.getApplication().invokeLater(
                () -> consoleView.print(message + "\n", ConsoleViewContentType.NORMAL_OUTPUT)
        );
    }

    @Override
    public void dispose() {
        stopWatching();
    }

}
