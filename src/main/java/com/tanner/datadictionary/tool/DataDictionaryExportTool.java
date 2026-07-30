package com.tanner.datadictionary.tool;

import com.tanner.base.DbUtil;
import com.tanner.datadictionary.engine.IEngine;
import com.tanner.datadictionary.entity.AggTable;
import com.tanner.datadictionary.entity.ColumnInfo;
import com.tanner.datadictionary.entity.TableInfo;
import com.intellij.openapi.progress.ProgressIndicator;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class DataDictionaryExportTool {

    private Connection connection;
    private ProgressIndicator progressIndicator;

    public DataDictionaryExportTool() {
    }

    public DataDictionaryExportTool(Connection connection, ProgressIndicator progressIndicator) {
        this.connection = connection;
        this.progressIndicator = progressIndicator;
    }

    public void export(String exportDirPath, List<TableInfo> selectedTables, String exportAs, boolean needFilterDefField) throws Exception {
        progressIndicator.setText("正在查询数据字典");
        progressIndicator.setIndeterminate(selectedTables.isEmpty());
        List<AggTable> aggTableList = buildAggTables(selectedTables, needFilterDefField);
        progressIndicator.setText("正在组装导出数据");
        IExportBuilder exportBuilder = getExportBuilder(exportAs);
        if (exportBuilder == null) {
            throw new IllegalArgumentException("不支持的导出格式: " + exportAs);
        }
        exportBuilder.build(aggTableList, exportDirPath);
        progressIndicator.setFraction(1);
    }

    private List<AggTable> buildAggTables(List<TableInfo> selectedTables, boolean needFilterDefField) throws Exception {
        List<AggTable> aggTableList = new ArrayList<>();
        IEngine engine = DbUtil.getEngine(connection);
        for (TableInfo selectedTable : selectedTables) {
            int currentIndex = selectedTables.indexOf(selectedTable) + 1;
            progressIndicator.checkCanceled();
            progressIndicator.setText(
                    "正在查询数据字典(" + currentIndex + "/" + selectedTables.size() + ")");
            AggTable aggTable = new AggTable();
            aggTable.setTableInfo(selectedTable);
            List<ColumnInfo> columnInfoList = engine.getAllColumnInfo(
                    connection, selectedTable.getTableName(), needFilterDefField,
                    progressIndicator);
            aggTable.setColumnInfoList(columnInfoList);
            aggTableList.add(aggTable);
            progressIndicator.setFraction((double) currentIndex / selectedTables.size());
        }
        return aggTableList;
    }

    private IExportBuilder getExportBuilder(String exportAs) {
        return switch (exportAs) {
            case "pdf" -> new PdfBuilder();
            case "markdown" -> new MarkdownBuilder();
            case "html" -> new HtmlBuilder();
            default -> null;
        };
    }

}
