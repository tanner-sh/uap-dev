package com.tanner.datadictionary.tool;

import com.tanner.datadictionary.entity.AggTable;
import com.tanner.datadictionary.entity.ColumnInfo;
import com.tanner.datadictionary.entity.TableInfo;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;

public class HtmlBuilder implements IExportBuilder {

    @Override
    public void build(List<AggTable> aggTableList, String exportDirPath) throws Exception {
        String filePath = Path.of(exportDirPath, "datadictionary.html").toString();
        String markdownContent = getMarkdownContent(aggTableList);
        File scriptFile = new File(filePath);
        scriptFile.deleteOnExit();
        scriptFile.createNewFile();
        FileUtils.writeStringToFile(scriptFile, markdownContent, Charset.defaultCharset());
    }

    private String getMarkdownContent(List<AggTable> aggTableList) {
        StringBuffer htmlBuilder = new StringBuffer();
        // 在 StringBuilder 中添加 HTML 标签和内容
        htmlBuilder.append("<html><body>");
        // 添加标题
        htmlBuilder.append("<h1>数据字典</h1>");
        for (AggTable aggTable : aggTableList) {
            Integer index = aggTableList.indexOf(aggTable) + 1;
            TableInfo tableInfo = aggTable.getTableInfo();
            List<ColumnInfo> columnInfoList = aggTable.getColumnInfoList();
            StringBuffer oneTableContent = new StringBuffer();
            //拼接二级标题
            oneTableContent.append("<h2>");
            oneTableContent.append(index).append(".").append(tableInfo.getTableName()).append(" ");
            oneTableContent.append(tableInfo.getComment());
            //拼接表格
            //表格头
            oneTableContent.append("<table border=\"1\" cellspacing=\"0\">");
            oneTableContent.append("<thead>");
            oneTableContent.append("<tr><th>序列</th><th>列名</th><th>类型</th><th>可空</th><th>默认值</th><th>注释</th><th>枚举</th></tr>");
            oneTableContent.append("</thead>");
            //表格体
            oneTableContent.append("<thead>");
            for (int k = 0; k < columnInfoList.size(); k++) {
                ColumnInfo columnInfo = columnInfoList.get(k);
                oneTableContent.append("<tr>");
                oneTableContent.append("<th>").append(columnInfo.getColumnId()).append("</th>");
                oneTableContent.append("<th>").append(columnInfo.getColumnName()).append("</th>");
                oneTableContent.append("<th>").append(columnInfo.getType()).append("</th>");
                oneTableContent.append("<th>").append(columnInfo.getNullAble()).append("</th>");
                oneTableContent.append("<th>").append(columnInfo.getDefaultValue()).append("</th>");
                oneTableContent.append("<th>").append(columnInfo.getComment()).append("</th>");
                oneTableContent.append("<th>").append(columnInfo.getEnumValue()).append("</th>");
                oneTableContent.append("</tr>");
            }
            oneTableContent.append("</thead>");
            oneTableContent.append("</table>");
            htmlBuilder.append(oneTableContent);
        }
        htmlBuilder.append("</body></html>");
        return htmlBuilder.toString();
    }

}
