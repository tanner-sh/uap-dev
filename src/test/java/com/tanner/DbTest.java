package com.tanner;

import com.tanner.base.ClassLoaderUtil;

import java.sql.*;
import java.util.Properties;

public class DbTest {

    @org.junit.Test
    public void getColumnNames() throws Exception {
        String homePath = "/Users/tanner/Documents/yonyou/nchomes/ncc2111";
        ClassLoader classLoader = ClassLoaderUtil.getUapJdbcClassLoader(homePath);
        String driverClass = "oracle.jdbc.driver.OracleDriver";
        String userName = "ncc2111";
        String pwd = "1";
        String jdbcUrl = "jdbc:oracle:thin:@127.0.0.1:1521:orcl";
        Class<?> driverClazz = classLoader.loadClass(driverClass);
        Driver deiver = (Driver) driverClazz.getConstructor().newInstance();
        Properties properties = new Properties();
        properties.put("user", userName);
        properties.put("password", pwd);
        Connection connection = deiver.connect(jdbcUrl, properties);
        String querySql = "select * from SIM_STOCK_IDISTILL";
        PreparedStatement preparedStatement = connection.prepareStatement(querySql);
        ResultSet resultSet = preparedStatement.executeQuery();
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        for (int i = 1; i < resultSetMetaData.getColumnCount(); i++) {
            System.out.println(resultSetMetaData.getColumnName(i));
        }
    }

}
