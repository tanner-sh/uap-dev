package com.tanner.datadictionary.engine;

import com.tanner.datadictionary.entity.ColumnInfo;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class MySqlEngineTest {

    @Test
    public void loadsAllColumnEnumsWithOneMetadataQuery() throws Exception {
        AtomicInteger enumQueries = new AtomicInteger();

        List<ColumnInfo> columns = new MySqlEngine().getAllColumnInfo(
                connectionWithMetadataEnums(enumQueries), "DEMO", false);

        assertEquals(2, columns.size());
        assertEquals("0=停用;\n1=启用;", columns.get(0).getEnumValue());
        assertEquals("A=甲;", columns.get(1).getEnumValue());
        assertEquals(1, enumQueries.get());
    }

    private Connection connectionWithMetadataEnums(AtomicInteger enumQueries) {
        return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class[]{Connection.class}, (proxy, method, args) -> {
                    if ("getCatalog".equals(method.getName())) {
                        return "demo";
                    }
                    if ("prepareStatement".equals(method.getName())) {
                        return preparedStatement((String) args[0], enumQueries);
                    }
                    return defaultValue(method.getReturnType());
                });
    }

    private PreparedStatement preparedStatement(String sql, AtomicInteger enumQueries) {
        return (PreparedStatement) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class[]{PreparedStatement.class}, (proxy, method, args) -> {
                    if ("executeQuery".equals(method.getName())) {
                        if (sql.toUpperCase().contains("INFORMATION_SCHEMA.COLUMNS")) {
                            return resultSet(List.of(
                                    row("COLUMN_NAME", "STATUS", "COLUMN_ID", 1,
                                            "DATA_TYPE", "tinyint", "NULLABLE", "YES",
                                            "DATA_DEFAULT", null, "COMMENTS", "状态"),
                                    row("COLUMN_NAME", "TYPE", "COLUMN_ID", 2,
                                            "DATA_TYPE", "varchar(20)", "NULLABLE", "YES",
                                            "DATA_DEFAULT", null, "COMMENTS", "类型")));
                        }
                        if (sql.toUpperCase().contains("JOIN MD_ENUMVALUE")) {
                            enumQueries.incrementAndGet();
                            return resultSet(List.of(
                                    row("PROPERTY_NAME", "STATUS", "ENUM_VALUE", "0",
                                            "ENUM_NAME", "停用"),
                                    row("PROPERTY_NAME", "STATUS", "ENUM_VALUE", "1",
                                            "ENUM_NAME", "启用"),
                                    row("PROPERTY_NAME", "TYPE", "ENUM_VALUE", "A",
                                            "ENUM_NAME", "甲")));
                        }
                        return resultSet(List.of());
                    }
                    return defaultValue(method.getReturnType());
                });
    }

    private ResultSet resultSet(List<Map<String, Object>> rows) {
        List<String> columns = rows.isEmpty() ? List.of()
                : List.copyOf(rows.get(0).keySet());
        ResultSetMetaData metadata = (ResultSetMetaData) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class[]{ResultSetMetaData.class},
                (proxy, method, args) -> {
                    if ("getColumnCount".equals(method.getName())) {
                        return columns.size();
                    }
                    if ("getColumnLabel".equals(method.getName())) {
                        return columns.get((Integer) args[0] - 1);
                    }
                    return defaultValue(method.getReturnType());
                });
        int[] index = {-1};
        return (ResultSet) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class[]{ResultSet.class}, (proxy, method, args) -> {
                    if ("next".equals(method.getName())) {
                        return ++index[0] < rows.size();
                    }
                    if ("getMetaData".equals(method.getName())) {
                        return metadata;
                    }
                    if ("getObject".equals(method.getName())) {
                        return rows.get(index[0]).get(columns.get((Integer) args[0] - 1));
                    }
                    return defaultValue(method.getReturnType());
                });
    }

    private Map<String, Object> row(Object... values) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            row.put((String) values[i], values[i + 1]);
        }
        return row;
    }

    private Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == boolean.class) {
            return false;
        }
        if (type == char.class) {
            return '\0';
        }
        return 0;
    }
}
