package com.tanner.base;

import com.tanner.datadictionary.engine.IEngine;
import com.tanner.datadictionary.engine.MySqlEngine;
import com.tanner.datadictionary.engine.OracleEngine;

import java.sql.*;
import java.util.*;

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
            List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            while (resultSet.next()) {
                Map<String, Object> map = new HashMap<String, Object>();
                for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
                    String columnName = resultSetMetaData.getColumnName(i);
                    Object columnValue = resultSet.getObject(columnName);
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
        List<String> exportSqls = new ArrayList<String>();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement(querySql);
            if (paramList != null) {
                for (int i = 0; i < paramList.size(); i++) {
                    preparedStatement.setObject(i + 1, paramList.get(i));
                }
            }
            resultSet = preparedStatement.executeQuery();
            List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            while (resultSet.next()) {
                StringBuilder exportSql = new StringBuilder("insert into ").append(tableName).append(" ");
                StringBuilder columnNames = new StringBuilder("(");
                StringBuilder columnValues = new StringBuilder("(");
                Map<String, Object> map = new HashMap<String, Object>();
                for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
                    String columnName = resultSetMetaData.getColumnName(i);
                    int columnType = resultSetMetaData.getColumnType(i);
                    Object columnValue = resultSet.getObject(columnName);
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
        } finally {
            closeResource(null, preparedStatement, resultSet);
        }
    }

    public static String getColumnValue(int columnType, Object columnValue) {
        if (Objects.isNull(columnValue)) {
            return null;
        }
        return switch (columnType) {
            case Types.VARCHAR, Types.CHAR, Types.LONGVARCHAR -> "'" + columnValue + "'";
            default -> columnValue.toString();
        };
    }

    public static IEngine getEngine(Connection connection) throws BusinessException {
        String databaseProductName = null;
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            databaseProductName = metaData.getDatabaseProductName();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return switch (databaseProductName) {
            case "Oracle" -> new OracleEngine();
            case "MySql" -> new MySqlEngine();
            default -> throw new BusinessException("不支持此数据库类型:" + databaseProductName);
        };
    }

    public static Connection getConnection(ClassLoader classLoader, String driverClass,
                                           String jdbcUrl, String userName, String pwd) throws BusinessException {
        Connection connection = null;
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
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
