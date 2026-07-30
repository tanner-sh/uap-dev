package com.tanner.devconfig.util;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.swing.table.DefaultTableModel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TableModelUtilTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void loadsSortedModuleRowsAndRestoresSelections() throws Exception {
        Path home = temporaryFolder.getRoot().toPath();
        createModule(home, "z-module");
        createModule(home, "a-module");
        Files.createDirectories(home.resolve("modules/not-a-module"));

        TableModelUtil.ModuleTableData data = TableModelUtil.loadModuleData(
                home.toString(), "a-module", "z-module");

        assertEquals(2, data.mustRows().size());
        assertEquals("a-module", data.mustRows().get(0)[2]);
        assertEquals("z-module", data.mustRows().get(1)[2]);
        assertTrue((Boolean) data.mustRows().get(0)[1]);
        assertFalse((Boolean) data.selectedRows().get(1)[1]);
    }

    @Test
    public void collectsModuleStateByNameWhenTableOrdersDiffer() {
        DefaultTableModel mustModel = new DefaultTableModel(
                new Object[][]{
                        {1, true, "a-module"},
                        {2, false, "b-module"}
                },
                new Object[]{"序号", "选中", "模块名称"});
        DefaultTableModel selectedModel = new DefaultTableModel(
                new Object[][]{
                        {2, false, "b-module"},
                        {1, true, "a-module"}
                },
                new Object[]{"序号", "启动", "模块名称"});

        TableModelUtil.ModuleSelection selection =
                TableModelUtil.collectModuleSelection(mustModel, selectedModel);

        assertEquals("a-module", selection.mustModules());
        assertEquals("b-module", selection.excludedModules());
    }

    private static void createModule(Path home, String name) throws Exception {
        Path metadata = home.resolve("modules").resolve(name).resolve("META-INF");
        Files.createDirectories(metadata);
        Files.writeString(metadata.resolve("module.xml"),
                "<module name=\"" + name + "\"/>", StandardCharsets.UTF_8);
    }
}
