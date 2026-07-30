package com.tanner.base;

import com.intellij.openapi.progress.ProgressIndicator;
import com.tanner.datadictionary.engine.IEngine;
import com.tanner.datadictionary.engine.MySqlEngine;
import com.tanner.datadictionary.engine.OracleEngine;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.TreeMap;

public class DbUtil {

    public static List<Map<String, Object>> executeQuery(Connection connection, String sql,
                                                         List<Object> paramList) throws BusinessException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            if (paramList != null) {
                for (int i = 0; i < paramList.size(); i++) {
                    preparedStatement.setObject(i + 1, paramList.get(i));
                }
            }
            resultSet = preparedStatement.executeQuery();
            List<Map<String, Object>> resultList = new ArrayList<>();
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            while (resultSet.next()) {
                Map<String, Object> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
                for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
                    String columnName = resultSetMetaData.getColumnLabel(i);
                    Object columnValue = resultSet.getObject(i);
                    map.put(columnName, columnValue);
                }
                resultList.add(map);
            }
            return resultList;
        } catch (SQLException e) {
            throw new BusinessException("查询失败:" + e.getMessage());
        } finally {
            closeResource(null, preparedStatement, resultSet);
        }
    }

    public static List<String> getInsertScripts(Connection connection, String tableName,
                                                String querySql, List<Object> paramList, boolean spiltGo) throws BusinessException {
        return getInsertScripts(connection, tableName, querySql, paramList, spiltGo, null);
    }

    public static List<String> getInsertScripts(Connection connection, String tableName,
                                                String querySql, List<Object> paramList,
                                                boolean spiltGo,
                                                ProgressIndicator indicator)
            throws BusinessException {
        List<String> exportSqls = new ArrayList<>();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            checkCanceled(indicator);
            preparedStatement = connection.prepareStatement(querySql);
            if (paramList != null) {
                for (int i = 0; i < paramList.size(); i++) {
                    checkCanceled(indicator);
                    preparedStatement.setObject(i + 1, paramList.get(i));
                }
            }
            resultSet = preparedStatement.executeQuery();
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            while (resultSet.next()) {
                checkCanceled(indicator);
                StringBuilder exportSql = new StringBuilder("insert into ").append(tableName).append(" ");
                StringBuilder columnNames = new StringBuilder("(");
                StringBuilder columnValues = new StringBuilder("(");
                for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
                    String columnName = resultSetMetaData.getColumnLabel(i);
                    int columnType = resultSetMetaData.getColumnType(i);
                    Object columnValue = resultSet.getObject(i);
                    columnNames.append(columnName).append(",");
                    columnValues.append(getColumnValue(columnType, columnValue)).append(",");
                }
                columnNames.deleteCharAt(columnNames.length() - 1).append(")");
                columnValues.deleteCharAt(columnValues.length() - 1).append(")");
                exportSql.append(columnNames).append(" values ").append(columnValues).append(";");
                if (spiltGo) {
                    exportSql.append("\n").append("go").append("\n");
                }
                exportSqls.add(exportSql.toString());
            }
            return exportSqls;
        } catch (SQLException e) {
            throw new BusinessException("查询失败:" + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("生成插入脚本失败:" + e.getMessage());
        } finally {
            closeResource(null, preparedStatement, resultSet);
        }
    }

    private static void checkCanceled(ProgressIndicator indicator) {
        if (indicator != null) {
            indicator.checkCanceled();
        }
    }

    public static String getColumnValue(int columnType, Object columnValue) {
        if (Objects.isNull(columnValue)) {
            return "NULL";
        }
        return switch (columnType) {
            case Types.VARCHAR, Types.CHAR, Types.LONGVARCHAR,
                    Types.NVARCHAR, Types.NCHAR, Types.LONGNVARCHAR,
                    Types.DATE, Types.TIME, Types.TIMESTAMP,
                    Types.TIME_WITH_TIMEZONE, Types.TIMESTAMP_WITH_TIMEZONE ->
                    quoteSqlValue(columnValue.toString());
            case Types.CLOB, Types.NCLOB -> quoteSqlValue(readTextValue(columnValue));
            case Types.BOOLEAN, Types.BIT ->
                    columnValue instanceof Boolean value ? (value ? "1" : "0")
                            : columnValue.toString();
            case Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY, Types.BLOB ->
                    "X'" + HexFormat.of().formatHex(readBinaryValue(columnValue)) + "'";
            default -> columnValue.toString();
        };
    }

    private static String readTextValue(Object value) {
        if (!(value instanceof Clob clob)) {
            return value.toString();
        }
        try (Reader reader = clob.getCharacterStream()) {
            StringBuilder content = new StringBuilder();
            char[] buffer = new char[4096];
            int length;
            while ((length = reader.read(buffer)) != -1) {
                content.append(buffer, 0, length);
            }
            return content.toString();
        } catch (SQLException | IOException e) {
            throw new IllegalArgumentException("读取 CLOB 字段失败", e);
        }
    }

    private static byte[] readBinaryValue(Object value) {
        if (value instanceof byte[] bytes) {
            return bytes;
        }
        if (value instanceof Blob blob) {
            try (InputStream input = blob.getBinaryStream()) {
                return input.readAllBytes();
            } catch (SQLException | IOException e) {
                throw new IllegalArgumentException("读取 BLOB 字段失败", e);
            }
        }
        throw new IllegalArgumentException("不支持的二进制字段类型: "
                + value.getClass().getName());
    }

    private static String quoteSqlValue(String value) {
        return "'" + value.replace("'", "''") + "'";
    }

    public static IEngine getEngine(Connection connection) throws BusinessException {
        String databaseProductName;
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            databaseProductName = metaData.getDatabaseProductName();
        } catch (SQLException e) {
            throw new BusinessException("读取数据库类型失败: " + e.getMessage());
        }
        String normalizedName = databaseProductName == null ? ""
                : databaseProductName.toLowerCase();
        if (normalizedName.contains("oracle")) {
            return new OracleEngine();
        }
        if (normalizedName.contains("mysql") || normalizedName.contains("oceanbase")) {
            return new MySqlEngine();
        }
        throw new BusinessException("不支持此数据库类型:" + databaseProductName);
    }

    public static Connection getConnection(ClassLoader classLoader, String driverClass,
                                           String jdbcUrl, String userName, String pwd) throws BusinessException {
        Connection connection;
        try {
            Class<?> driverClazz = classLoader.loadClass(driverClass);
            Driver deiver = (Driver) driverClazz.getConstructor().newInstance();
            Properties properties = new Properties();
            properties.put("user", userName);
            properties.put("password", pwd);
            connection = deiver.connect(jdbcUrl, properties);
        } catch (Exception e) {
            throw new BusinessException("获取数据库连接失败!" + e.getMessage());
        }
        if (connection == null) {
            throw new BusinessException("获取数据库连接失败!");
        }
        return connection;
    }

    public static void closeResource(Connection connection, PreparedStatement preparedStatement,
                                     ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException ignored) {

            }
        }
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException ignored) {

            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {

            }
        }
    }

}
