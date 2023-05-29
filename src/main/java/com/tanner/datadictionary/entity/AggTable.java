package com.tanner.datadictionary.entity;

import java.util.List;

public class AggTable {

    private TableInfo tableInfo;
    private List<ColumnInfo> columnInfoList;

    public TableInfo getTableInfo() {
        return tableInfo;
    }

    public void setTableInfo(TableInfo tableInfo) {
        this.tableInfo = tableInfo;
    }

    public List<ColumnInfo> getColumnInfoList() {
        return columnInfoList;
    }

    public void setColumnInfoList(List<ColumnInfo> columnInfoList) {
        this.columnInfoList = columnInfoList;
    }

}
