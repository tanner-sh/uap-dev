package com.tanner.logwatcher;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.Assert.assertEquals;

public class LogWatcherServiceTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void discoversLogDirectoryAndFileCreatedAfterStartup() throws Exception {
        Path logDirectory = temporaryFolder.getRoot().toPath().resolve("nclogs");
        LogWatcherService watcher = new LogWatcherService();
        try {
            watcher.startWatching(logDirectory);
            Files.createDirectories(logDirectory.resolve("server"));
            Files.writeString(logDirectory.resolve("server/nc-log.log"), "启动成功\n",
                    Charset.forName("GB2312"));

            long deadline = System.currentTimeMillis() + 4000;
            while (watcher.watchedFileCount() == 0
                    && System.currentTimeMillis() < deadline) {
                Thread.sleep(50);
            }
            assertEquals(1, watcher.watchedFileCount());
            assertEquals(Charset.forName("GB2312"), watcher.logCharset());
        } finally {
            watcher.dispose();
        }
    }

    @Test
    public void drainsLogMessagesInOrderAndHonorsBatchLimit() {
        ConcurrentLinkedQueue<String> messages = new ConcurrentLinkedQueue<>();
        messages.add("one");
        messages.add("two");
        messages.add("three");

        assertEquals("one\ntwo\n", LogWatcherService.drainMessages(messages, 2));
        assertEquals("three\n", LogWatcherService.drainMessages(messages, 2));
    }
}
