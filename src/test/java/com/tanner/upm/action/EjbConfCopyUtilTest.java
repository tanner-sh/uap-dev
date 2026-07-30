package com.tanner.upm.action;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class EjbConfCopyUtilTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void copiesOnlyUpmAndRestFilesFromMetaInf() throws Exception {
        File source = temporaryFolder.newFolder("source");
        File metaInf = new File(source, "META-INF");
        assertTrue(metaInf.mkdirs());
        Files.writeString(new File(metaInf, "demo.upm").toPath(), "upm");
        Files.writeString(new File(metaInf, "demo.rest").toPath(), "rest");
        Files.writeString(new File(source, "ignored.upm").toPath(), "ignored");
        File home = temporaryFolder.newFolder("home");

        int copied = new EjbConfCopyUtil().copy(
                home.getPath(), "demo-module", source.getPath(), null);

        assertEquals(2, copied);
        assertTrue(new File(home, "modules/demo-module/META-INF/demo.upm").isFile());
        assertTrue(new File(home, "modules/demo-module/META-INF/demo.rest").isFile());
    }

    @Test
    public void rejectsModulePathTraversal() throws Exception {
        File source = temporaryFolder.newFolder("source-invalid");
        File home = temporaryFolder.newFolder("home-invalid");
        try {
            new EjbConfCopyUtil().copy(home.getPath(), "../outside", source.getPath(), null);
            fail("Path traversal should fail");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("非法 NC 模块名称"));
        }
    }
}
