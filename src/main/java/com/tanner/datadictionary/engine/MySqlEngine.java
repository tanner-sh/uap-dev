package com.tanner.datadictionary.engine;

import com.intellij.openapi.progress.ProgressIndicator;
import com.tanner.base.BusinessException;
import com.tanner.base.DbUtil;
import com.tanner.datadictionary.entity.ColumnInfo;
import com.tanner.datadictionary.entity.TableInfo;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class MySqlEngine implements IEngine {

    @Override
    public List<TableInfo> getAllTableInfo(Connection connection, String userName,
                                           String[] tableNamePattern,
                                           @Nullable ProgressIndicator indicator)
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
        for (Map<String, Object> row : DbUtil.executeQuery(
                connection, sql.toString(), parameters, indicator)) {
            String tableName = Objects.toString(row.get("TABLE_NAME"), "");
            String comment = getMetadataTableComment(connection, tableName, indicator);
            if (comment.isEmpty()) {
                comment = Objects.toString(row.get("TABLE_COMMENT"), "");
            }
            tables.add(new TableInfo(tableName, comment));
        }
        return tables;
    }

    @Override
    public List<ColumnInfo> getAllColumnInfo(Connection connection, String tableName,
                                             boolean needFilterDefField,
                                             @Nullable ProgressIndicator indicator)
            throws BusinessException {
        String sql = "SELECT COLUMN_NAME, ORDINAL_POSITION AS COLUMN_ID, "
                + "COLUMN_TYPE AS DATA_TYPE, IS_NULLABLE AS NULLABLE, "
                + "COLUMN_DEFAULT AS DATA_DEFAULT, COLUMN_COMMENT AS COMMENTS "
                + "FROM information_schema.COLUMNS "
                + "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? ORDER BY ORDINAL_POSITION";
        List<Object> parameters = List.of(getCatalog(connection), tableName);
        List<ColumnInfo> columns = new ArrayList<>();
        Pattern defaultFieldPattern = Pattern.compile("^def\\d+$", Pattern.CASE_INSENSITIVE);
        for (Map<String, Object> row : DbUtil.executeQuery(
                connection, sql, parameters, indicator)) {
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
            columns.add(column);
        }
        Map<String, String> enumValues = getEnumValuesFromMetadata(
                connection, tableName, indicator);
        for (ColumnInfo column : columns) {
            column.setEnumValue(enumValues.getOrDefault(
                    column.getColumnName().toUpperCase(Locale.ROOT), ""));
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

    private String getMetadataTableComment(Connection connection, String tableName,
                                           @Nullable ProgressIndicator indicator) {
        try {
            List<Map<String, Object>> rows = DbUtil.executeQuery(connection,
                    "SELECT DISPLAYNAME FROM MD_CLASS WHERE UPPER(DEFAULTTABLENAME) = ?",
                    Collections.singletonList(tableName.toUpperCase()), indicator);
            return rows.isEmpty() ? "" : Objects.toString(rows.get(0).get("DISPLAYNAME"), "");
        } catch (BusinessException ignored) {
            return "";
        }
    }

    private Map<String, String> getEnumValuesFromMetadata(Connection connection,
                                                          String tableName,
                                                          @Nullable ProgressIndicator indicator) {
        String sql = "SELECT P.NAME AS PROPERTY_NAME, E.VALUE AS ENUM_VALUE, "
                + "E.NAME AS ENUM_NAME FROM MD_PROPERTY P "
                + "JOIN MD_CLASS C ON C.ID = P.CLASSID "
                + "JOIN MD_ENUMVALUE E ON E.ID = P.DATATYPE "
                + "WHERE UPPER(C.DEFAULTTABLENAME) = ? ORDER BY P.NAME, E.VALUE";
        try {
            List<Map<String, Object>> rows = DbUtil.executeQuery(connection, sql,
                    Collections.singletonList(tableName.toUpperCase(Locale.ROOT)), indicator);
            Map<String, List<String>> valuesByColumn = new HashMap<>();
            for (Map<String, Object> row : rows) {
                String propertyName = Objects.toString(row.get("PROPERTY_NAME"), "");
                if (propertyName.isBlank()) {
                    continue;
                }
                valuesByColumn.computeIfAbsent(propertyName.toUpperCase(Locale.ROOT),
                        ignored -> new ArrayList<>()).add(
                        Objects.toString(row.get("ENUM_VALUE"), "") + "="
                                + Objects.toString(row.get("ENUM_NAME"), "") + ";");
            }
            Map<String, String> result = new HashMap<>();
            valuesByColumn.forEach((key, values) -> result.put(key, String.join("\n", values)));
            return result;
        } catch (BusinessException ignored) {
            return Collections.emptyMap();
        }
    }

}
