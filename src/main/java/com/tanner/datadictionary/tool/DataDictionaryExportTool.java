package com.tanner.datadictionary.tool;

import com.tanner.base.DbUtil;
import com.tanner.datadictionary.engine.IEngine;
import com.tanner.datadictionary.entity.AggTable;
import com.tanner.datadictionary.entity.ColumnInfo;
import com.tanner.datadictionary.entity.TableInfo;

import javax.swing.*;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class DataDictionaryExportTool {

    private Connection connection;
    private JProgressBar progressBar;

    public DataDictionaryExportTool() {
    }

    public DataDictionaryExportTool(Connection connection, JProgressBar progressBar) {
        this.connection = connection;
        this.progressBar = progressBar;
    }

    public void export(String exportDirPath, List<TableInfo> selectedTables, String exportAs, boolean needFilterDefField) throws Exception {
        progressBar.setString("正在查询数据字典!");
        progressBar.setMinimum(0);
        progressBar.setMaximum(selectedTables.size());
        List<AggTable> aggTableList = buildAggTables(selectedTables, needFilterDefField);
        progressBar.setString("正在组装导出数据!");
        IExportBuilder exportBuilder = getExportBuilder(exportAs);
        exportBuilder.build(aggTableList, exportDirPath);
        DbUtil.closeResource(connection, null, null);
        progressBar.setString("导出完毕!");
    }

    private List<AggTable> buildAggTables(List<TableInfo> selectedTables, boolean needFilterDefField) throws Exception {
        List<AggTable> aggTableList = new ArrayList<>();
        IEngine engine = DbUtil.getEngine(connection);
        for (TableInfo selectedTable : selectedTables) {
            int currentIndex = selectedTables.indexOf(selectedTable) + 1;
            progressBar.setString("正在查询数据字典(" + currentIndex + "/" + selectedTables.size() + ")");
            AggTable aggTable = new AggTable();
            aggTable.setTableInfo(selectedTable);
            List<ColumnInfo> columnInfoList = engine.getAllColumnInfo(connection, selectedTable.getTableName(), needFilterDefField);
            aggTable.setColumnInfoList(columnInfoList);
            aggTableList.add(aggTable);
            progressBar.setValue(currentIndex);
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
