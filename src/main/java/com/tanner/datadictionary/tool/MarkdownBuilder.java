package com.tanner.datadictionary.tool;

import com.tanner.datadictionary.entity.AggTable;
import com.tanner.datadictionary.entity.ColumnInfo;
import com.tanner.datadictionary.entity.TableInfo;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;

public class MarkdownBuilder implements IExportBuilder {

    @Override
    public void build(List<AggTable> aggTableList, String exportDirPath) throws Exception {
        String filePath = Path.of(exportDirPath, "datadictionary.md").toString();
        String markdownContent = getMarkdownContent(aggTableList);
        File scriptFile = new File(filePath);
        scriptFile.deleteOnExit();
        scriptFile.createNewFile();
        FileUtils.writeStringToFile(scriptFile, markdownContent, Charset.defaultCharset());
    }

    private String getMarkdownContent(List<AggTable> aggTableList) {
        StringBuilder markdownContent = new StringBuilder();
        String res1 = "|:------:|:------:|:------:|:------:|:------:|:------:|" + "\n";
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
                        append(columnInfo.getColumnName()).append("|").
                        append(columnInfo.getType()).append("|").
                        append(columnInfo.getNullAble()).append("|").
                        append(columnInfo.getDefaultValue()).append("|").
                        append(columnInfo.getComment()).append("|").
                        append(columnInfo.getEnumValue()).append("|").append("\n");
            }
            markdownContent.append(oneTableContent);
        }
        markdownContent.insert(0, "[TOC]\n");
        return markdownContent.toString();
    }

}
