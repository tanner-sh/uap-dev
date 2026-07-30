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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class LogWatcherService implements Disposable {

    private final String[] logFileNames = {"nc-log.log"};
    private final AtomicBoolean running = new AtomicBoolean();
    private final AtomicBoolean disposed = new AtomicBoolean();
    private ConsoleView consoleView; // 内置日志展示组件
    private final Charset charset = Charset.forName("GB2312");
    private final List<Tailer> tailers = new CopyOnWriteArrayList<>();
    private final Set<Path> watchedFiles = ConcurrentHashMap.newKeySet();
    private final ScheduledExecutorService scanner = Executors.newSingleThreadScheduledExecutor(
            runnable -> {
                Thread thread = new Thread(runnable, "uap-log-watcher-scanner");
                thread.setDaemon(true);
                return thread;
            });
    private volatile ScheduledFuture<?> scanTask;

    public void setConsoleView(ConsoleView consoleView) {
        this.consoleView = consoleView;
    }

    public void startWatching(Path rootDirectory) {
        if (rootDirectory == null) {
            appendLog("Log directory is not configured.");
            return;
        }
        if (disposed.get()) {
            return;
        }
        if (!running.compareAndSet(false, true)) {
            appendLog("Log watcher is already running.");
            return;
        }
        Path normalizedRoot = rootDirectory.toAbsolutePath().normalize();
        scanLogFiles(normalizedRoot, true);
        scanTask = scanner.scheduleWithFixedDelay(
                () -> scanLogFiles(normalizedRoot, false), 1, 1, TimeUnit.SECONDS);
        if (Files.isDirectory(normalizedRoot)) {
            appendLog("Log watcher started for directory: " + normalizedRoot);
        } else {
            appendLog("Waiting for log directory: " + normalizedRoot);
        }
    }

    private void scanLogFiles(Path rootDirectory, boolean startAtEnd) {
        if (!running.get() || !Files.isDirectory(rootDirectory)) {
            return;
        }
        try {
            Collection<File> logFiles = FileUtils.listFiles(rootDirectory.toFile(),
                    new String[]{"log"}, true);
            logFiles.stream()
                    .filter(logFile -> ArrayUtils.contains(logFileNames, logFile.getName()))
                    .forEach(file -> addLogFile(rootDirectory, file, startAtEnd));
        } catch (Exception e) {
            appendLog("Failed to scan log directory: " + e.getMessage());
        }
    }

    private synchronized void addLogFile(Path rootDirectory, File file, boolean startAtEnd) {
        Path filePath = file.toPath().toAbsolutePath().normalize();
        if (!running.get() || !watchedFiles.add(filePath)) {
            return;
        }
        String displayName;
        try {
            displayName = rootDirectory.relativize(filePath).toString();
        } catch (IllegalArgumentException ignored) {
            displayName = file.getName();
        }
        String logName = displayName;
        Tailer tailer = new Tailer(file, charset, new TailerListenerAdapter() {
            @Override
            public void handle(String line) {
                appendLog("[" + logName + "] " + line);
            }

            @Override
            public void handle(Exception exception) {
                appendLog("[" + logName + "] " + exception.getMessage());
            }
        }, 1000, startAtEnd, false, 8192);
        Thread thread = new Thread(tailer, "log-watcher-" + filePath.hashCode());
        thread.setDaemon(true);
        tailers.add(tailer);
        thread.start();
        appendLog("Watching log file: " + filePath);
    }

    public synchronized void stopWatching() {
        if (!running.compareAndSet(true, false)) {
            return;
        }
        ScheduledFuture<?> currentScanTask = scanTask;
        scanTask = null;
        if (currentScanTask != null) {
            currentScanTask.cancel(false);
        }
        for (Tailer tailer : new ArrayList<>(tailers)) {
            tailer.stop();
        }
        tailers.clear();
        watchedFiles.clear();
        appendLog("Log watcher stopped.");
    }

    public void appendLog(String message) {
        ConsoleView currentConsole = consoleView;
        if (currentConsole == null || disposed.get()) {
            return;
        }
        ApplicationManager.getApplication().invokeLater(
                () -> {
                    if (!disposed.get()) {
                        currentConsole.print(message + "\n",
                                ConsoleViewContentType.NORMAL_OUTPUT);
                    }
                }
        );
    }

    int watchedFileCount() {
        return watchedFiles.size();
    }

    Charset logCharset() {
        return charset;
    }

    @Override
    public void dispose() {
        disposed.set(true);
        stopWatching();
        scanner.shutdownNow();
        consoleView = null;
    }

}
