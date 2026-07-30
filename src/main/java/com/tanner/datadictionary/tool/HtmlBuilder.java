package com.tanner.datadictionary.tool;

import com.tanner.datadictionary.entity.AggTable;
import com.tanner.datadictionary.entity.ColumnInfo;
import com.tanner.datadictionary.entity.TableInfo;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class HtmlBuilder implements IExportBuilder {

    @Override
    public void build(List<AggTable> aggTableList, String exportDirPath) throws Exception {
        String filePath = Path.of(exportDirPath, "datadictionary.html").toString();
        String markdownContent = getMarkdownContent(aggTableList);
        Files.writeString(Path.of(filePath), markdownContent, StandardCharsets.UTF_8);
    }

    private String getMarkdownContent(List<AggTable> aggTableList) {
        StringBuilder htmlBuilder = new StringBuilder();
        // 在 StringBuilder 中添加 HTML 标签和内容
        htmlBuilder.append("<!doctype html><html><head><meta charset=\"UTF-8\"></head><body>");
        // 添加标题
        htmlBuilder.append("<h1>数据字典</h1>");
        for (AggTable aggTable : aggTableList) {
            Integer index = aggTableList.indexOf(aggTable) + 1;
            TableInfo tableInfo = aggTable.getTableInfo();
            List<ColumnInfo> columnInfoList = aggTable.getColumnInfoList();
            StringBuilder oneTableContent = new StringBuilder();
            //拼接二级标题
            oneTableContent.append("<h2>");
            oneTableContent.append(index).append(".").append(escapeHtml(tableInfo.getTableName())).append(" ");
            oneTableContent.append(escapeHtml(tableInfo.getComment())).append("</h2>");
            //拼接表格
            //表格头
            oneTableContent.append("<table border=\"1\" cellspacing=\"0\">");
            oneTableContent.append("<thead>");
            oneTableContent.append("<tr><th>序列</th><th>列名</th><th>类型</th><th>可空</th><th>默认值</th><th>注释</th><th>枚举</th></tr>");
            oneTableContent.append("</thead>");
            //表格体
            oneTableContent.append("<tbody>");
            for (ColumnInfo columnInfo : columnInfoList) {
                oneTableContent.append("<tr>");
                oneTableContent.append("<td>").append(columnInfo.getColumnId()).append("</td>");
                oneTableContent.append("<td>").append(escapeHtml(columnInfo.getColumnName())).append("</td>");
                oneTableContent.append("<td>").append(escapeHtml(columnInfo.getType())).append("</td>");
                oneTableContent.append("<td>").append(escapeHtml(columnInfo.getNullAble())).append("</td>");
                oneTableContent.append("<td>").append(escapeHtml(columnInfo.getDefaultValue())).append("</td>");
                oneTableContent.append("<td>").append(escapeHtml(columnInfo.getComment())).append("</td>");
                oneTableContent.append("<td>").append(escapeHtml(columnInfo.getEnumValue())).append("</td>");
                oneTableContent.append("</tr>");
            }
            oneTableContent.append("</tbody>");
            oneTableContent.append("</table>");
            htmlBuilder.append(oneTableContent);
        }
        htmlBuilder.append("</body></html>");
        return htmlBuilder.toString();
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

}
