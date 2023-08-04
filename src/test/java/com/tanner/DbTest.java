package com.tanner;

import com.tanner.base.ClassLoaderUtil;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Properties;

public class DbTest {

    @org.junit.Test
    public void getColumnNames() throws Exception {
        String homePath = "/Users/tanner/Documents/yonyou/uaphomes/ncc2111";
        ClassLoader classLoader = ClassLoaderUtil.getUapJdbcClassLoader(homePath);
        String driverClass = "dm.jdbc.driver.DmDriver";
        String userName = "SYSDBA";
        String pwd = "SYSDBA";
        String jdbcUrl = "jdbc:dm://localhost:5236?keyWords=LOGIC";
        Class<?> driverClazz = classLoader.loadClass(driverClass);
        Driver deiver = (Driver) driverClazz.getConstructor().newInstance();
        Properties properties = new Properties();
        properties.put("user", userName);
        properties.put("password", pwd);
        Connection connection = deiver.connect(jdbcUrl, properties);
        String querySql = "select si_datasource.pk_datasource pk_datasource, si_datasource.ts ts, si_datasource.dr dr, si_datasource.field field, si_datasource.logic logic, si_datasource.pk_detailtemplet pk_detailtemplet, si_datasource.table_name table_name from si_datasource si_datasource where pk_detailtemplet = '1001A110000000005I5S'";
        PreparedStatement preparedStatement = connection.prepareStatement(querySql);
        ResultSet resultSet = preparedStatement.executeQuery();
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        for (int i = 1; i < resultSetMetaData.getColumnCount(); i++) {
            System.out.println(resultSetMetaData.getColumnName(i));
        }
    }

}
