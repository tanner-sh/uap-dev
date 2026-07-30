package com.tanner.datadictionary.tool;

import com.tanner.datadictionary.entity.AggTable;
import com.tanner.datadictionary.entity.ColumnInfo;
import com.tanner.datadictionary.entity.TableInfo;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.parser.PdfTextExtractor;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class PdfBuilderTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void exportsExtractableChineseWithEmbeddedFont() throws Exception {
        AggTable table = new AggTable();
        table.setTableInfo(new TableInfo("用户表", "用户基础信息"));
        table.setColumnInfoList(List.of(new ColumnInfo(
                1, "姓名", "varchar(50)", "Y", "", "中文姓名", "")));

        new PdfBuilder().build(List.of(table), temporaryFolder.getRoot().getPath());

        File pdf = new File(temporaryFolder.getRoot(), "datadictionary.pdf");
        byte[] bytes = Files.readAllBytes(pdf.toPath());
        PdfReader reader = new PdfReader(bytes);
        try {
            assertTrue(reader.getNumberOfPages() > 0);
            StringBuilder extracted = new StringBuilder();
            PdfTextExtractor extractor = new PdfTextExtractor(reader);
            for (int page = 1; page <= reader.getNumberOfPages(); page++) {
                extracted.append(extractor.getTextFromPage(page));
            }
            assertTrue(extracted.toString(), extracted.toString().contains("用户"));
        } finally {
            reader.close();
        }
        String rawPdf = new String(bytes, StandardCharsets.ISO_8859_1);
        assertTrue("The Chinese font must be embedded",
                rawPdf.contains("/FontFile2") || rawPdf.contains("/FontFile3"));
    }
}
