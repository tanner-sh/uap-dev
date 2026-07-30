package com.tanner.datadictionary.engine;

import com.tanner.base.BusinessException;
import com.tanner.base.DbUtil;
import com.tanner.datadictionary.entity.ColumnInfo;
import com.tanner.datadictionary.entity.TableInfo;
import org.apache.commons.lang3.ArrayUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class MySqlEngine implements IEngine {

    @Override
    public List<TableInfo> getAllTableInfo(Connection connection, String userName,
                                           String[] tableNamePattern)
            throws BusinessException {
        String catalog = getCatalog(connection);
        StringBuilder sql = new StringBuilder(
                "SELECT TABLE_NAME, TABLE_COMMENT FROM information_schema.TABLES "
                        + "WHERE TABLE_SCHEMA = ? AND TABLE_TYPE = 'BASE TABLE'");
        List<Object> parameters = new ArrayList<>();
        parameters.add(catalog);
        if (!ArrayUtils.isEmpty(tableNamePattern)) {
            sql.append(" AND (1 = 2");
            for (String ignored : tableNamePattern) {
                sql.append(" OR UPPER(TABLE_NAME) LIKE ?");
            }
            sql.append(")");
            for (String pattern : tableNamePattern) {
                parameters.add(pattern.toUpperCase());
            }
        }
        sql.append(" ORDER BY TABLE_NAME");

        List<TableInfo> tables = new ArrayList<>();
        for (Map<String, Object> row : DbUtil.executeQuery(connection, sql.toString(), parameters)) {
            String tableName = Objects.toString(row.get("TABLE_NAME"), "");
            String comment = getMetadataTableComment(connection, tableName);
            if (comment.isEmpty()) {
                comment = Objects.toString(row.get("TABLE_COMMENT"), "");
            }
            tables.add(new TableInfo(tableName, comment));
        }
        return tables;
    }

    @Override
    public List<ColumnInfo> getAllColumnInfo(Connection connection, String tableName,
                                             boolean needFilterDefField)
            throws BusinessException {
        String sql = "SELECT COLUMN_NAME, ORDINAL_POSITION AS COLUMN_ID, "
                + "COLUMN_TYPE AS DATA_TYPE, IS_NULLABLE AS NULLABLE, "
                + "COLUMN_DEFAULT AS DATA_DEFAULT, COLUMN_COMMENT AS COMMENTS "
                + "FROM information_schema.COLUMNS "
                + "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? ORDER BY ORDINAL_POSITION";
        List<Object> parameters = List.of(getCatalog(connection), tableName);
        List<ColumnInfo> columns = new ArrayList<>();
        Pattern defaultFieldPattern = Pattern.compile("^def\\d+$", Pattern.CASE_INSENSITIVE);
        for (Map<String, Object> row : DbUtil.executeQuery(connection, sql, parameters)) {
            String columnName = Objects.toString(row.get("COLUMN_NAME"), "");
            if (needFilterDefField && defaultFieldPattern.matcher(columnName).matches()) {
                continue;
            }
            ColumnInfo column = new ColumnInfo();
            Object columnId = row.get("COLUMN_ID");
            column.setColumnId(columnId instanceof Number number ? number.intValue() : 0);
            column.setColumnName(columnName);
            column.setType(Objects.toString(row.get("DATA_TYPE"), ""));
            column.setNullAble(Objects.toString(row.get("NULLABLE"), ""));
            column.setDefaultValue(Objects.toString(row.get("DATA_DEFAULT"), ""));
            column.setComment(Objects.toString(row.get("COMMENTS"), ""));
            column.setEnumValue(getEnumValueFromMetadata(connection, tableName, columnName));
            columns.add(column);
        }
        return columns;
    }

    private String getCatalog(Connection connection) throws BusinessException {
        try {
            String catalog = connection.getCatalog();
            if (catalog == null || catalog.isBlank()) {
                throw new BusinessException("无法确定当前 MySQL 数据库名称");
            }
            return catalog;
        } catch (SQLException e) {
            throw new BusinessException("读取 MySQL 数据库名称失败: " + e.getMessage());
        }
    }

    private String getMetadataTableComment(Connection connection, String tableName) {
        try {
            List<Map<String, Object>> rows = DbUtil.executeQuery(connection,
                    "SELECT DISPLAYNAME FROM MD_CLASS WHERE UPPER(DEFAULTTABLENAME) = ?",
                    Collections.singletonList(tableName.toUpperCase()));
            return rows.isEmpty() ? "" : Objects.toString(rows.get(0).get("DISPLAYNAME"), "");
        } catch (BusinessException ignored) {
            return "";
        }
    }

    private String getEnumValueFromMetadata(Connection connection, String tableName,
                                            String columnName) {
        String sql = "SELECT VALUE, NAME FROM MD_ENUMVALUE "
                + "WHERE ID = (SELECT DATATYPE FROM MD_PROPERTY "
                + "WHERE UPPER(NAME) = ? AND CLASSID = "
                + "(SELECT ID FROM MD_CLASS WHERE UPPER(DEFAULTTABLENAME) = ?)) "
                + "ORDER BY VALUE";
        try {
            List<Map<String, Object>> rows = DbUtil.executeQuery(connection, sql,
                    List.of(columnName.toUpperCase(), tableName.toUpperCase()));
            List<String> values = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                values.add(Objects.toString(row.get("VALUE"), "") + "="
                        + Objects.toString(row.get("NAME"), ""));
            }
            return String.join(";\n", values);
        } catch (BusinessException ignored) {
            return "";
        }
    }

}
