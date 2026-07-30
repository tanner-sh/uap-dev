package com.tanner.datadictionary.tool;

import com.tanner.datadictionary.entity.AggTable;
import com.tanner.datadictionary.entity.ColumnInfo;
import com.tanner.datadictionary.entity.TableInfo;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class MarkdownBuilder implements IExportBuilder {

    @Override
    public void build(List<AggTable> aggTableList, String exportDirPath) throws Exception {
        String filePath = Path.of(exportDirPath, "datadictionary.md").toString();
        String markdownContent = getMarkdownContent(aggTableList);
        Files.writeString(Path.of(filePath), markdownContent, StandardCharsets.UTF_8);
    }

    private String getMarkdownContent(List<AggTable> aggTableList) {
        StringBuilder markdownContent = new StringBuilder();
        String res1 = "|:------:|:------:|:------:|:------:|:------:|:------:|:------:|" + "\n";
        for (AggTable aggTable : aggTableList) {
            int index = aggTableList.indexOf(aggTable) + 1;
            TableInfo tableInfo = aggTable.getTableInfo();
            List<ColumnInfo> columnInfoList = aggTable.getColumnInfoList();
            StringBuilder oneTableContent = new StringBuilder();
            oneTableContent.append("## ").append(index).append(".").append(tableInfo.getTableName()).append(" ").append(tableInfo.getComment()).append("\n\n").append("|序列|列名|类型|可空|默认值|注释|枚举|").append("\n");
            oneTableContent.append(res1);
            //拼接列
            for (ColumnInfo columnInfo : columnInfoList) {
                oneTableContent.append("|").append(columnInfo.getColumnId()).append("|").
                        append(escapeCell(columnInfo.getColumnName())).append("|").
                        append(escapeCell(columnInfo.getType())).append("|").
                        append(escapeCell(columnInfo.getNullAble())).append("|").
                        append(escapeCell(columnInfo.getDefaultValue())).append("|").
                        append(escapeCell(columnInfo.getComment())).append("|").
                        append(escapeCell(columnInfo.getEnumValue())).append("|").append("\n");
            }
            markdownContent.append(oneTableContent);
        }
        markdownContent.insert(0, "[TOC]\n");
        return markdownContent.toString();
    }

    static String escapeCell(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("|", "\\|")
                .replace("\r\n", "<br>")
                .replace("\n", "<br>")
                .replace("\r", "<br>");
    }

}
