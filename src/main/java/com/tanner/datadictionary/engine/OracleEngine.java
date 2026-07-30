package com.tanner.datadictionary.engine;

import com.intellij.openapi.progress.ProgressIndicator;
import com.tanner.base.BusinessException;
import com.tanner.base.DbUtil;
import com.tanner.datadictionary.entity.ColumnInfo;
import com.tanner.datadictionary.entity.TableInfo;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class OracleEngine implements IEngine {

    private static final int ORACLE_IN_CLAUSE_BATCH_SIZE = 900;

    @Override
    public List<TableInfo> getAllTableInfo(Connection connection, String userName,
                                           String[] tableNamePattern,
                                           @Nullable ProgressIndicator indicator)
            throws BusinessException {
        List<TableInfo> tableInfoList = new ArrayList<>();
        StringBuilder querySql = new StringBuilder("select TABLE_NAME,COMMENTS from USER_TAB_COMMENTS WHERE 1=1");
        List<Object> parameters = new ArrayList<>();
        if (!ArrayUtils.isEmpty(tableNamePattern)) {
            querySql.append(" AND ( 1 = 2 ");
            for (String key : tableNamePattern) {
                querySql.append(" or upper(TABLE_NAME) LIKE ?");
                parameters.add(key.toUpperCase());
            }
            querySql.append(" ) ");
        }
        querySql.append(" ORDER BY TABLE_NAME");
        List<Map<String, Object>> queryResult = DbUtil.executeQuery(connection,
                querySql.toString(), parameters, indicator);
        for (Map<String, Object> stringObjectMap : queryResult) {
            String tableName = (String) stringObjectMap.get("TABLE_NAME");
            tableInfoList.add(new TableInfo(tableName,
                    (String) stringObjectMap.get("COMMENTS")));
        }
        applyTableCommentsFromMD(connection, tableInfoList, indicator);
        return tableInfoList;
    }

    private void applyTableCommentsFromMD(Connection connection,
                                          List<TableInfo> tableInfoList,
                                          @Nullable ProgressIndicator indicator) {
        if (tableInfoList.isEmpty()) {
            return;
        }
        Map<String, TableInfo> tablesByName = new HashMap<>();
        for (TableInfo tableInfo : tableInfoList) {
            tablesByName.put(tableInfo.getTableName().toUpperCase(Locale.ROOT), tableInfo);
        }
        for (int start = 0; start < tableInfoList.size();
             start += ORACLE_IN_CLAUSE_BATCH_SIZE) {
            int end = Math.min(start + ORACLE_IN_CLAUSE_BATCH_SIZE,
                    tableInfoList.size());
            List<Object> parameters = new ArrayList<>(end - start);
            for (int index = start; index < end; index++) {
                parameters.add(tableInfoList.get(index).getTableName()
                        .toUpperCase(Locale.ROOT));
            }
            String placeholders = String.join(",",
                    Collections.nCopies(parameters.size(), "?"));
            String querySql = "SELECT UPPER(DEFAULTTABLENAME) AS TABLE_NAME, "
                    + "MAX(DISPLAYNAME) AS DISPLAYNAME FROM MD_CLASS "
                    + "WHERE UPPER(DEFAULTTABLENAME) IN (" + placeholders + ") "
                    + "GROUP BY UPPER(DEFAULTTABLENAME)";
            try {
                for (Map<String, Object> row : DbUtil.executeQuery(
                        connection, querySql, parameters, indicator)) {
                    String tableName = Objects.toString(row.get("TABLE_NAME"), "");
                    String displayName = Objects.toString(row.get("DISPLAYNAME"), "");
                    TableInfo tableInfo = tablesByName.get(
                            tableName.toUpperCase(Locale.ROOT));
                    if (tableInfo != null && StringUtils.isNotEmpty(displayName)) {
                        tableInfo.setComment(displayName);
                    }
                }
            } catch (BusinessException ignored) {
                // 部分项目没有 NC 元数据表，保留 USER_TAB_COMMENTS 中的标准备注。
                return;
            }
        }
    }

    @Override
    public List<ColumnInfo> getAllColumnInfo(Connection connection, String tableName,
                                             boolean needFilterDefField,
                                             @Nullable ProgressIndicator indicator)
            throws BusinessException {
        List<ColumnInfo> columnInfoList = new ArrayList<>();
        StringBuilder querySql = new StringBuilder("select COLUMN_NAME, COLUMN_ID, DATA_TYPE, NULLABLE, DATA_DEFAULT");
        querySql.append(" from USER_TAB_COLUMNS where TABLE_NAME = ?");
        List<Map<String, Object>> queryResult = DbUtil.executeQuery(
                connection, querySql.toString(), Collections.singletonList(tableName), indicator);
        for (Map<String, Object> rowMap : queryResult) {
            ColumnInfo columnInfo = new ColumnInfo();
            columnInfo.setColumnId(((BigDecimal) rowMap.get("COLUMN_ID")).intValue());
            columnInfo.setColumnName((String) rowMap.get("COLUMN_NAME"));
            columnInfo.setType((String) rowMap.get("DATA_TYPE"));
            columnInfo.setNullAble((String) rowMap.get("NULLABLE"));
            columnInfo.setDefaultValue((String) rowMap.get("DATA_DEFAULT"));
            columnInfoList.add(columnInfo);
        }
        if (needFilterDefField) {//过滤自定义字段
            Pattern pattern = Pattern.compile("^def\\d+");
            List<ColumnInfo> filteredList = columnInfoList.stream()
                    .filter(columnInfo -> !pattern.matcher(columnInfo.getColumnName().toLowerCase()).matches())
                    .toList();
            columnInfoList.clear();
            columnInfoList.addAll(filteredList);
        }
        // 先从元数据中获取字段备注信息
        queryResult = getColumnCommentsFromMD(connection, tableName, indicator);
        if (!queryResult.isEmpty()) {
            for (Map<String, Object> rowMap : queryResult) {
                String name = (String) rowMap.get("NAME");
                String displayname = (String) rowMap.get("DISPLAYNAME");
                columnInfoList.stream()
                        .filter(columnInfo -> columnInfo.getColumnName().equalsIgnoreCase(name))
                        .forEach(columnInfo -> columnInfo.setComment(displayname));
            }
        } else {
            querySql = new StringBuilder("SELECT COLUMN_NAME, COMMENTS FROM USER_COL_COMMENTS");
            querySql.append(" WHERE TABLE_NAME = ?");
            queryResult = DbUtil.executeQuery(connection, querySql.toString(),
                    Collections.singletonList(tableName), indicator);
            for (Map<String, Object> rowMap : queryResult) {
                String column_name = (String) rowMap.get("COLUMN_NAME");
                String comments = (String) rowMap.get("COMMENTS");
                columnInfoList.stream()
                        .filter(columnInfo -> columnInfo.getColumnName().equals(column_name))
                        .forEach(columnInfo -> columnInfo.setComment(comments));
            }
        }
        // 从元数据中获取枚举信息
        Map<String, String> enumValues = getEnumValuesFromMD(
                connection, tableName, indicator);
        for (ColumnInfo columnInfo : columnInfoList) {
            columnInfo.setEnumValue(enumValues.getOrDefault(
                    columnInfo.getColumnName().toUpperCase(Locale.ROOT), ""));
        }
        return columnInfoList;
    }

    private List<Map<String, Object>> getColumnCommentsFromMD(Connection connection,
                                                              String tableName,
                                                              @Nullable ProgressIndicator indicator) {
        try {
            return DbUtil.executeQuery(connection,
                    "SELECT NAME,DISPLAYNAME FROM MD_COLUMN WHERE UPPER(TABLEID) = ?",
                    Collections.singletonList(tableName.toUpperCase()), indicator);
        } catch (BusinessException ignored) {
            return Collections.emptyList();
        }
    }

    private Map<String, String> getEnumValuesFromMD(Connection connection, String tableName,
                                                    @Nullable ProgressIndicator indicator) {
        //TODO 这个sql有点问题 元数据字段不一定是和数据库字段名一致的
        String querySql = "SELECT P.NAME AS PROPERTY_NAME, E.VALUE AS ENUM_VALUE, "
                + "E.NAME AS ENUM_NAME FROM MD_PROPERTY P "
                + "JOIN MD_CLASS C ON C.ID = P.CLASSID "
                + "JOIN MD_ENUMVALUE E ON E.ID = P.DATATYPE "
                + "WHERE UPPER(C.DEFAULTTABLENAME) = ? ORDER BY P.NAME, E.VALUE";
        try {
            Map<String, List<String>> valuesByColumn = new HashMap<>();
            for (Map<String, Object> row : DbUtil.executeQuery(connection, querySql,
                    Collections.singletonList(tableName.toUpperCase(Locale.ROOT)), indicator)) {
                String propertyName = Objects.toString(row.get("PROPERTY_NAME"), "");
                if (propertyName.isBlank()) {
                    continue;
                }
                String value = Objects.toString(row.get("ENUM_VALUE"), "");
                String name = Objects.toString(row.get("ENUM_NAME"), "");
                valuesByColumn.computeIfAbsent(propertyName.toUpperCase(Locale.ROOT),
                        ignored -> new ArrayList<>()).add(value + "=" + name + ";");
            }
            Map<String, String> result = new HashMap<>();
            valuesByColumn.forEach((key, values) -> result.put(key, String.join("\n", values)));
            return result;
        } catch (BusinessException ignored) {
            return Collections.emptyMap();
        }
    }

}
