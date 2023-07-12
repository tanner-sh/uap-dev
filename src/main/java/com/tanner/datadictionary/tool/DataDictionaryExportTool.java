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

    private JTextField logTextField;

    public DataDictionaryExportTool() {
    }

    public DataDictionaryExportTool(Connection connection, JTextField logTextField) {
        this.connection = connection;
        this.logTextField = logTextField;
    }

    public void export(String exportDirPath, List<TableInfo> selectedTables, String exportAs, boolean needFilterDefField) throws Exception {
        List<AggTable> aggTableList = buildAggTables(selectedTables, needFilterDefField);
        IExportBuilder exportBuilder = getExportBuilder(exportAs);
        logTextField.setText("正在组装导出文件!");
        exportBuilder.build(aggTableList, exportDirPath);
        logTextField.setText("组装导出文件完毕!");
        DbUtil.closeResource(connection, null, null);
        logTextField.setText("导出完成!");
    }

    private List<AggTable> buildAggTables(List<TableInfo> selectedTables, boolean needFilterDefField) throws Exception {
        List<AggTable> aggTableList = new ArrayList<>();
        IEngine engine = DbUtil.getEngine(connection);
        for (TableInfo selectedTable : selectedTables) {
            StringBuilder logText = new StringBuilder("正在读取相关信息:" + selectedTable.getTableName());
            logText.append(",进度[" + selectedTables.indexOf(selectedTable) + 1 + "/" + selectedTables.size() + "]");
            logTextField.setText(logText.toString());
            AggTable aggTable = new AggTable();
            aggTable.setTableInfo(selectedTable);
            List<ColumnInfo> columnInfoList = engine.getAllColumnInfo(connection, selectedTable.getTableName(), needFilterDefField);
            aggTable.setColumnInfoList(columnInfoList);
            aggTableList.add(aggTable);
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
