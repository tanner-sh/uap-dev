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
        StringBuffer markdownContent = new StringBuffer();
        String res1 = "|:------:|:------:|:------:|:------:|:------:|:------:|" + "\n";
        for (AggTable aggTable : aggTableList) {
            Integer index = aggTableList.indexOf(aggTable) + 1;
            TableInfo tableInfo = aggTable.getTableInfo();
            List<ColumnInfo> columnInfoList = aggTable.getColumnInfoList();
            StringBuffer oneTableContent = new StringBuffer();
            oneTableContent.append("## " + index + "." + tableInfo.getTableName() + " " + tableInfo.getComment() + "\n\n" + "|序列|列名|类型|可空|默认值|注释|" + "\n");
            oneTableContent.append(res1);
            //拼接列
            for (int k = 0; k < columnInfoList.size(); k++) {
                ColumnInfo columnInfo = columnInfoList.get(k);
                oneTableContent.append("|").append(columnInfo.getColumnId()).append("|").
                        append(columnInfo.getColumnName()).append("|").
                        append(columnInfo.getType()).append("|").
                        append(columnInfo.getNullAble()).append("|").
                        append(columnInfo.getDefaultValue()).append("|").
                        append(columnInfo.getComment()).append("|").append("\n");
            }
            markdownContent.append(oneTableContent);
        }
        markdownContent.insert(0, "[TOC]\n");
        return markdownContent.toString();
    }

}
