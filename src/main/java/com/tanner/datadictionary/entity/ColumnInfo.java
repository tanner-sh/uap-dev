package com.tanner.datadictionary.entity;

import org.apache.commons.lang3.StringUtils;

public class ColumnInfo {

    private int columnId;//序号
    private String columnName;//列名
    private String type;//数据库类型
    private String nullAble;//可空
    private String defaultValue;//默认值
    private String comment;//注释
    private String enumValue;//枚举

    public ColumnInfo() {
    }

    public ColumnInfo(int columnId, String columnName, String type, String nullAble, String defaultValue, String comment, String enumValue) {
        this.columnId = columnId;
        this.columnName = columnName;
        this.type = type;
        this.nullAble = nullAble;
        this.defaultValue = defaultValue;
        this.comment = comment;
        this.enumValue = enumValue;
    }

    public int getColumnId() {
        return columnId;
    }

    public void setColumnId(int columnId) {
        this.columnId = columnId;
    }

    public String getColumnName() {
        return StringUtils.defaultString(columnName);
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getType() {
        return StringUtils.defaultString(type);
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNullAble() {
        return StringUtils.defaultString(nullAble);
    }

    public void setNullAble(String nullAble) {
        this.nullAble = nullAble;
    }

    public String getDefaultValue() {
        return StringUtils.defaultString(defaultValue);
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getComment() {
        return StringUtils.defaultString(comment);
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getEnumValue() {
        return enumValue;
    }

    public void setEnumValue(String enumValue) {
        this.enumValue = enumValue;
    }

    @Override
    public String toString() {
        return "ColumnInfo{" +
                "columnId=" + columnId +
                ", columnName='" + columnName + '\'' +
                ", type='" + type + '\'' +
                ", nullAble='" + nullAble + '\'' +
                ", defaultValue='" + defaultValue + '\'' +
                ", comment='" + comment + '\'' +
                ", enumValue='" + enumValue + '\'' +
                '}';
    }
}
