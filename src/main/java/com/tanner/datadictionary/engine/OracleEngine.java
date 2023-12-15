package com.tanner.datadictionary.engine;

import com.tanner.base.BusinessException;
import com.tanner.base.DbUtil;
import com.tanner.datadictionary.entity.ColumnInfo;
import com.tanner.datadictionary.entity.TableInfo;
import org.apache.commons.lang3.ArrayUtils;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OracleEngine implements IEngine {

    @Override
    public List<TableInfo> getAllTableInfo(Connection connection, String userName, String[] tableNamePattern) throws BusinessException {
        List<TableInfo> tableInfoList = new ArrayList<>();
        StringBuilder querySql = new StringBuilder("select TABLE_NAME,COMMENTS from USER_TAB_COMMENTS WHERE 1=1");
        if (!ArrayUtils.isEmpty(tableNamePattern)) {
            querySql.append(" AND ( 1 = 2 ");
            for (String key : tableNamePattern) {
                querySql.append(" or upper(TABLE_NAME) LIKE '" + key.toUpperCase() + "'");
            }
            querySql.append(" ) ");
        }
        querySql.append(" ORDER BY TABLE_NAME");
        List<Map<String, Object>> queryResult = DbUtil.executeQuery(connection, querySql.toString(), null);
        for (Map<String, Object> stringObjectMap : queryResult) {
            String tableName = (String) stringObjectMap.get("TABLE_NAME");
            String comments = (String) stringObjectMap.get("COMMENTS");
            tableInfoList.add(new TableInfo(tableName, comments));
        }
        return tableInfoList;
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
        for (ColumnInfo columnInfo : columnInfoList) {
            String enumValue = getEnumValueFromMD(connection, tableName, columnInfo.getColumnName());
            columnInfo.setEnumValue(enumValue);
        }
        return columnInfoList;
    }

    private String getEnumValueFromMD(Connection connection, String tableName, String columnName) throws BusinessException {
        //TODO 这个sql有点问题 元数据字段不一定是和数据库字段名一致的
        StringBuilder enumValue = new StringBuilder();
        StringBuilder querySql = new StringBuilder("select VALUE, NAME from MD_ENUMVALUE");
        querySql.append(" where id = (select DATATYPE from MD_PROPERTY");
        querySql.append(" where upper(name) = ?");
        querySql.append(" and CLASSID = (select id from md_class where upper(DEFAULTTABLENAME) = ?))");
        querySql.append(" order by VALUE");
        List<Map<String, Object>> queryResult = DbUtil.executeQuery(
                connection,
                querySql.toString(),
                Stream.of(columnName.toUpperCase(), tableName.toUpperCase()).collect(Collectors.toList()));
        for (Map<String, Object> rowMap : queryResult) {
            String value = (String) rowMap.get("VALUE");
            String name = (String) rowMap.get("NAME");
            enumValue.append(value).append("=").append(name).append(";");
            if (queryResult.indexOf(rowMap) != queryResult.size()) {
                enumValue.append("\n");
            }
        }
        return enumValue.toString();
    }

}
