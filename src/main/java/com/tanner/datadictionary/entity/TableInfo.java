package com.tanner.datadictionary.entity;

import org.apache.commons.lang.StringUtils;

public class TableInfo {

    private String tableName;
    private String comment;

    public TableInfo() {
    }

    public TableInfo(String tableName, String comment) {
        this.tableName = tableName;
        this.comment = comment;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getComment() {
        return StringUtils.defaultString(comment);
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "TableInfo{" +
                "tableName='" + tableName + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }

}
