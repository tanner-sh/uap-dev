package com.tanner;

import com.tanner.base.BusinessException;
import com.tanner.base.ConfigureFileUtil;
import com.tanner.datadictionary.entity.AggTable;
import com.tanner.datadictionary.entity.ColumnInfo;
import com.tanner.datadictionary.entity.TableInfo;
import com.tanner.datadictionary.tool.HtmlBuilder;
import com.tanner.datadictionary.tool.MarkdownBuilder;
import com.tanner.patcher.action.ZipUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.zip.ZipFile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ExportFileTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void exportedDictionaryFilesRemainOnDiskAndEscapeHtml() throws Exception {
        File output = temporaryFolder.newFolder("dictionary");
        ColumnInfo column = new ColumnInfo(1, "name", "varchar", "YES", null,
                "<script>|下一行\n内容", "");
        AggTable table = new AggTable();
        table.setTableInfo(new TableInfo("demo", "A&B"));
        table.setColumnInfoList(List.of(column));

        new MarkdownBuilder().build(List.of(table), output.getPath());
        new HtmlBuilder().build(List.of(table), output.getPath());

        File markdown = new File(output, "datadictionary.md");
        File html = new File(output, "datadictionary.html");
        assertTrue(markdown.isFile());
        assertTrue(html.isFile());
        assertTrue(Files.readString(markdown.toPath()).contains(
                "|:------:|:------:|:------:|:------:|:------:|:------:|:------:|"));
        assertTrue(Files.readString(markdown.toPath(), StandardCharsets.UTF_8)
                .contains("&lt;script&gt;\\|下一行<br>内容")
                || Files.readString(markdown.toPath(), StandardCharsets.UTF_8)
                .contains("<script>\\|下一行<br>内容"));
        String htmlText = Files.readString(html.toPath());
        assertTrue(htmlText.contains("A&amp;B"));
        assertTrue(htmlText.contains("&lt;script&gt;"));
        assertTrue(htmlText.contains("</h2>"));
        assertTrue(htmlText.contains("<meta charset=\"UTF-8\">"));
        assertTrue(htmlText.contains("<td>"));
    }

    @Test
    public void zipUsesRelativeEntriesAndRejectsPathTraversal() throws Exception {
        File staging = temporaryFolder.newFolder("patch");
        File nested = new File(staging, "replacement/modules/demo");
        assertTrue(nested.mkdirs());
        Files.writeString(new File(nested, "Demo.class").toPath(), "data");

        String zipName = ZipUtil.toZip(staging.getPath(), "safe");
        try (ZipFile zipFile = new ZipFile(zipName)) {
            assertTrue(zipFile.getEntry("replacement/modules/demo/Demo.class") != null);
        }

        try {
            ZipUtil.toZip(staging.getPath(), "../outside");
            fail("Path traversal should fail");
        } catch (BusinessException expected) {
            assertTrue(expected.getMessage().contains("invalid patch name"));
        }
    }

    @Test
    public void checkedFileWriterCreatesParentAndReportsContent() throws Exception {
        File output = new File(temporaryFolder.getRoot(), "nested/result.txt");
        new ConfigureFileUtil().outFile(output, "内容", StandardCharsets.UTF_8.name(),
                false);
        assertEquals("内容", Files.readString(output.toPath()));
    }
}
