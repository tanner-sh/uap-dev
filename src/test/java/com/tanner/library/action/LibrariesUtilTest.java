package com.tanner.library.action;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class LibrariesUtilTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void confinesExtractedJarEntriesToOutputRoot() throws Exception {
        Path root = temporaryFolder.newFolder("extend").toPath();
        assertEquals(root.resolve("yyconfig/demo.xml").toAbsolutePath().normalize(),
                LibrariesUtil.resolveExtractionTarget(root, "yyconfig/demo.xml"));

        try {
            LibrariesUtil.resolveExtractionTarget(root,
                    "yyconfig/../../../../outside.xml");
            fail("Path traversal should be rejected");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("越出目标目录"));
        }
    }
}
