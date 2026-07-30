package com.tanner;

import com.tanner.base.UapUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;

public class UapUtilTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void readsNumericAndNcCloudVersionsFromTemporaryHome() throws Exception {
        File home = temporaryFolder.newFolder("home");
        File setup = new File(home, "ncscript/uapServer/setup.ini");
        Files.createDirectories(setup.getParentFile().toPath());

        Files.writeString(setup.toPath(), "version=6.5.0\n");
        assertEquals("nc65", UapUtil.getUapVersion(home.getPath()));

        Files.writeString(setup.toPath(), "version=nccloud.2021.11\n");
        assertEquals("ncc2111", UapUtil.getUapVersion(home.getPath()));
    }

    @Test
    public void returnsUnknownWhenSetupFileIsMissing() throws Exception {
        File home = temporaryFolder.newFolder("empty-home");
        assertEquals("unknown", UapUtil.getUapVersion(home.getPath()));
    }
}
