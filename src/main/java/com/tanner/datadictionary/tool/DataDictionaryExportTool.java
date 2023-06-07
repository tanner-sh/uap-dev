package com.tanner.datadictionary.tool;

import com.tanner.base.DbUtil;
import com.tanner.datadictionary.engine.IEngine;
import com.tanner.datadictionary.entity.AggTable;
import com.tanner.datadictionary.entity.ColumnInfo;
import com.tanner.datadictionary.entity.TableInfo;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class DataDictionaryExportTool {

    private Connection connection;

    public DataDictionaryExportTool() {
    }

    public DataDictionaryExportTool(Connection connection) {
        this.connection = connection;
    }

    public void export(String exportDirPath, List<TableInfo> selectedTables, String exportAs) throws Exception {
        List<AggTable> aggTableList = buildAggTables(selectedTables);
        IExportBuilder exportBuilder = getExportBuilder(exportAs);
        exportBuilder.build(aggTableList, exportDirPath);
        DbUtil.closeResource(connection, null, null);
    }

    private List<AggTable> buildAggTables(List<TableInfo> selectedTables) throws Exception {
        List<AggTable> aggTableList = new ArrayList<>();
        IEngine engine = DbUtil.getEngine(connection);
        for (TableInfo selectedTable : selectedTables) {
            AggTable aggTable = new AggTable();
            aggTable.setTableInfo(selectedTable);
            List<ColumnInfo> columnInfoList = engine.getAllColumnInfo(connection, selectedTable.getTableName());
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
