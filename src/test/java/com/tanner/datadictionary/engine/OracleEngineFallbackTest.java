package com.tanner.datadictionary.engine;

import com.tanner.datadictionary.entity.ColumnInfo;
import com.tanner.datadictionary.entity.TableInfo;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class OracleEngineFallbackTest {

    @Test
    public void fallsBackToOracleCommentsWhenMetadataTablesAreUnavailable() throws Exception {
        OracleEngine engine = new OracleEngine();
        Connection connection = connectionWithoutMetadataTables();

        List<TableInfo> tables = engine.getAllTableInfo(connection, "DEMO", null);
        assertEquals(1, tables.size());
        assertEquals("标准表备注", tables.get(0).getComment());

        List<ColumnInfo> columns = engine.getAllColumnInfo(connection, "DEMO", false);
        assertEquals(1, columns.size());
        assertEquals("标准字段备注", columns.get(0).getComment());
        assertEquals("", columns.get(0).getEnumValue());
    }

    private Connection connectionWithoutMetadataTables() {
        return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class[]{Connection.class}, (proxy, method, args) -> {
                    if ("prepareStatement".equals(method.getName())) {
                        return preparedStatement((String) args[0]);
                    }
                    return defaultValue(method.getReturnType());
                });
    }

    private PreparedStatement preparedStatement(String sql) {
        return (PreparedStatement) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class[]{PreparedStatement.class}, (proxy, method, args) -> {
                    if ("executeQuery".equals(method.getName())) {
                        if (sql.toUpperCase().contains("MD_")) {
                            throw new SQLException("metadata table unavailable");
                        }
                        if (sql.toUpperCase().contains("USER_TAB_COMMENTS")) {
                            return resultSet(List.of(row(
                                    "TABLE_NAME", "DEMO",
                                    "COMMENTS", "标准表备注")));
                        }
                        if (sql.toUpperCase().contains("USER_TAB_COLUMNS")) {
                            return resultSet(List.of(row(
                                    "COLUMN_NAME", "CODE",
                                    "COLUMN_ID", BigDecimal.ONE,
                                    "DATA_TYPE", "VARCHAR2",
                                    "NULLABLE", "Y",
                                    "DATA_DEFAULT", null)));
                        }
                        if (sql.toUpperCase().contains("USER_COL_COMMENTS")) {
                            return resultSet(List.of(row(
                                    "COLUMN_NAME", "CODE",
                                    "COMMENTS", "标准字段备注")));
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
