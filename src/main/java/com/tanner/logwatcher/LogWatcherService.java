package com.tanner.logwatcher;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

public class LogWatcherService {
    private WatchService watchService;
    private boolean isRunning = false;
    private ConsoleView consoleView; // 内置日志展示组件
    private final Map<Path, Long> fileOffsets = new HashMap<>();

    public void setConsoleView(ConsoleView consoleView) {
        this.consoleView = consoleView;
    }

    public void startWatching(Path rootDirectory, Charset charset) {
        if (isRunning) {
            appendLog("Log watcher is already running.");
            return;
        }
        isRunning = true;

        try {
            watchService = FileSystems.getDefault().newWatchService();
            registerAllDirectories(rootDirectory);
            new Thread(() -> {
                try {
                    monitor(rootDirectory, charset);
                } catch (IOException | InterruptedException e) {
                    appendLog("Error while watching logs: " + e.getMessage());
                }
            }).start();

            appendLog("Log watcher started for directory: " + rootDirectory);
        } catch (IOException e) {
            appendLog("Failed to start log watcher: " + e.getMessage());
        }
    }

    private void registerAllDirectories(Path rootDirectory) throws IOException {
        Files.walkFileTree(rootDirectory, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                registerDirectory(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void registerDirectory(Path dir) throws IOException {
        dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
        appendLog("Monitoring directory: " + dir);
    }

    private void monitor(Path rootDirectory, Charset charset) throws IOException, InterruptedException {
        while (isRunning) {
            WatchKey key = watchService.take();
            Path dir = (Path) key.watchable();
            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                Path fileName = (Path) event.context();
                Path fullPath = dir.resolve(fileName);

                if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                    if (Files.isDirectory(fullPath)) {
                        registerDirectory(fullPath);
                    } else if (fullPath.toString().endsWith(".log")) {
                        appendLog("New log file detected: " + fullPath);
                        fileOffsets.put(fullPath, 0L);
                    }
                } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                    if (Files.isRegularFile(fullPath) && fullPath.toString().endsWith(".log")) {
                        readNewLogContent(fullPath, charset);
                    }
                } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                    appendLog("Log file deleted: " + fullPath);
                    fileOffsets.remove(fullPath);
                }
            }
            key.reset();
        }
    }

    private void readNewLogContent(Path file, Charset charset) throws IOException {
        Long offset = fileOffsets.getOrDefault(file, 0L);
        try (RandomAccessFile raf = new RandomAccessFile(file.toFile(), "r")) {
            if (raf.length() < offset) {
                appendLog("Log file reset detected: " + file);
                offset = 0L;
            }
            raf.seek(offset); // 定位到上次读取位置
            String line;
            while ((line = raf.readLine()) != null) {
                String decodedLine = new String(line.getBytes("ISO-8859-1"), charset);
                appendLog("[" + file.getFileName() + "] " + decodedLine);
            }
            fileOffsets.put(file, raf.getFilePointer());
        }
    }

    public void appendLog(String message) {
        if (consoleView != null) {
            consoleView.print(message + "\n", ConsoleViewContentType.NORMAL_OUTPUT);
        }
    }

}