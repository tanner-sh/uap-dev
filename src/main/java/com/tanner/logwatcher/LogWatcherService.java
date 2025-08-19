package com.tanner.logwatcher;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;

public class LogWatcherService {

    private final String[] logFileNames = {"nc-log.log"};
    private boolean isRunning = false; // 是否正在运行
    private ConsoleView consoleView; // 内置日志展示组件
    private final Charset charset = StandardCharsets.UTF_8;

    public void setConsoleView(ConsoleView consoleView) {
        this.consoleView = consoleView;
    }

    public void startWatching(Path rootDirectory) {
        if (isRunning) {
            appendLog("Log watcher is already running.");
            return;
        }
        isRunning = true;
        Collection<File> logFiles = FileUtils.listFiles(rootDirectory.toFile(), new String[]{"log"}, true);
        logFiles.stream().filter(logFile -> ArrayUtils.contains(logFileNames, logFile.getName())).forEach(this::addLogFile);
        appendLog("Log watcher started for directory: " + rootDirectory);
    }

    public void addLogFile(File file) {
        Tailer tailer = new Tailer(file, charset, new TailerListenerAdapter() {
            @Override
            public void handle(String line) {
                appendLog("[" + file.getName() + "] " + line);
            }
        }, 1000, false, false, 8192);
        new Thread(tailer).start();
    }

    public void appendLog(String message) {
        if (consoleView != null) {
            consoleView.print(message + "\n", ConsoleViewContentType.NORMAL_OUTPUT);
        }
    }

}