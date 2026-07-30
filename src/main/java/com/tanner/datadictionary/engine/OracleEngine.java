package com.tanner.datadictionary.engine;

import com.tanner.base.BusinessException;
import com.tanner.base.DbUtil;
import com.tanner.datadictionary.entity.ColumnInfo;
import com.tanner.datadictionary.entity.TableInfo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

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

    @Override
    public List<TableInfo> getAllTableInfo(Connection connection, String userName, String[] tableNamePattern) throws BusinessException {
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
                querySql.toString(), parameters);
        for (Map<String, Object> stringObjectMap : queryResult) {
            String tableName = (String) stringObjectMap.get("TABLE_NAME");
            // 优先从元数据信息中获取
            String comments = getTableCommentsFromMD(connection, tableName);
            if (StringUtils.isEmpty(comments)) {
                comments = (String) stringObjectMap.get("COMMENTS");
            }
            tableInfoList.add(new TableInfo(tableName, comments));
        }
        return tableInfoList;
    }

    private String getTableCommentsFromMD(Connection connection, String tableName) {
        try {
            String querySql = "select DISPLAYNAME from MD_CLASS where UPPER(DEFAULTTABLENAME) = ? ";
            List<Map<String, Object>> queryResult = DbUtil.executeQuery(connection, querySql,
                    Collections.singletonList(tableName.toUpperCase()));
            return CollectionUtils.isEmpty(queryResult) ? ""
                    : Objects.toString(queryResult.get(0).get("DISPLAYNAME"), "");
        } catch (BusinessException ignored) {
            return "";
        }
    }

    @Override
    public List<ColumnInfo> getAllColumnInfo(Connection connection, String tableName, boolean needFilterDefField) throws BusinessException {
        List<ColumnInfo> columnInfoList = new ArrayList<>();
        StringBuilder querySql = new StringBuilder("select COLUMN_NAME, COLUMN_ID, DATA_TYPE, NULLABLE, DATA_DEFAULT");
        querySql.append(" from USER_TAB_COLUMNS where TABLE_NAME = ?");
        List<Map<String, Object>> queryResult = DbUtil.executeQuery(connection, querySql.toString(), Collections.singletonList(tableName));
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
        queryResult = getColumnCommentsFromMD(connection, tableName);
        if (CollectionUtils.isNotEmpty(queryResult)) {
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
            queryResult = DbUtil.executeQuery(connection, querySql.toString(), Collections.singletonList(tableName));
            for (Map<String, Object> rowMap : queryResult) {
                String column_name = (String) rowMap.get("COLUMN_NAME");
                String comments = (String) rowMap.get("COMMENTS");
                columnInfoList.stream()
                        .filter(columnInfo -> columnInfo.getColumnName().equals(column_name))
                        .forEach(columnInfo -> columnInfo.setComment(comments));
            }
        }
        // 从元数据中获取枚举信息
        Map<String, String> enumValues = getEnumValuesFromMD(connection, tableName);
        for (ColumnInfo columnInfo : columnInfoList) {
            columnInfo.setEnumValue(enumValues.getOrDefault(
                    columnInfo.getColumnName().toUpperCase(Locale.ROOT), ""));
        }
        return columnInfoList;
    }

    private List<Map<String, Object>> getColumnCommentsFromMD(Connection connection,
                                                              String tableName) {
        try {
            return DbUtil.executeQuery(connection,
                    "SELECT NAME,DISPLAYNAME FROM MD_COLUMN WHERE UPPER(TABLEID) = ?",
                    Collections.singletonList(tableName.toUpperCase()));
        } catch (BusinessException ignored) {
            return Collections.emptyList();
        }
    }

    private Map<String, String> getEnumValuesFromMD(Connection connection, String tableName) {
        //TODO 这个sql有点问题 元数据字段不一定是和数据库字段名一致的
        String querySql = "SELECT P.NAME AS PROPERTY_NAME, E.VALUE AS ENUM_VALUE, "
                + "E.NAME AS ENUM_NAME FROM MD_PROPERTY P "
                + "JOIN MD_CLASS C ON C.ID = P.CLASSID "
                + "JOIN MD_ENUMVALUE E ON E.ID = P.DATATYPE "
                + "WHERE UPPER(C.DEFAULTTABLENAME) = ? ORDER BY P.NAME, E.VALUE";
        try {
            Map<String, List<String>> valuesByColumn = new HashMap<>();
            for (Map<String, Object> row : DbUtil.executeQuery(connection, querySql,
                    Collections.singletonList(tableName.toUpperCase(Locale.ROOT)))) {
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
