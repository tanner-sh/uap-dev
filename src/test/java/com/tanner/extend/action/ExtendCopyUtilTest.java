package com.tanner.extend.action;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExtendCopyUtilTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void preservesPathBelowYyconfigWhenCopying() throws Exception {
        File source = temporaryFolder.newFolder("module");
        File authorization = new File(source, "yyconfig/modules/demo/action");
        assertTrue(authorization.mkdirs());
        Files.writeString(new File(authorization, "authorize.xml").toPath(), "<x/>");
        Files.writeString(new File(authorization, "ignored.txt").toPath(), "ignored");
        File home = temporaryFolder.newFolder("home");

        int copied = ExtendCopyUtil.copyToNCHome(
                home.getPath(), source.toPath(), null);

        assertEquals(1, copied);
        assertTrue(new File(home,
                "hotwebs/nccloud/WEB-INF/extend/yyconfig/modules/demo/action/authorize.xml")
                .isFile());
    }
}
