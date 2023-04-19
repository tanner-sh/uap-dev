package com.tanner;

import com.tanner.base.ClassLoaderUtil;
import com.tanner.base.UapUtil;
import com.tanner.devconfig.util.AESEncode;

import java.io.File;
import java.sql.*;
import java.util.Properties;


public class Test {

//    @org.junit.Test
//    public void getUapVersion() throws Exception {
//        String[] homes = new String[]{
//                "D:\\yonyou\\nchome\\nchome57",
//                "D:\\yonyou\\nchome\\nchome_dw616\\nchome_dw",
//                "D:\\yonyou\\nchome\\nchomencc1909_htzq",
//                "D:\\yonyou\\nchome\\ncchome_ncc2005",
//                "D:\\yonyou\\nchome\\NCC2105-minihome",
//                "D:\\yonyou\\nchome\\ncc2111",
//        };
//        for (String home : homes) {
//            System.out.println(UapUtil.getUapVersion(home));
//        }
//        for (String home : homes) {
//            File propFile = new File(home, "/ierp/bin/key.properties");
//            if (propFile.exists()) {
//                System.out.println(home);
//            }
//        }
//    }
//
//    @org.junit.Test
//    public void encode() throws Exception {
//        System.out.println(AESEncode.encrypt("1", "D:\\yonyou\\nchome\\ncc2111"));
//    }
//
//    @org.junit.Test
//    public void decode() throws Exception {
//        System.out.println(AESEncode.decrypt("#9B8A8D3C81B33E7E1DD5F10BF93CFDCF", "D:\\yonyou\\nchome\\ncc2111"));
//    }
//
//    @org.junit.Test
//    public void copyFile() throws Exception {
//        String userName = System.getProperties().getProperty("user.name", "unknown");
//        System.out.println(userName);
//    }
//
//    @org.junit.Test
//    public void getColumnNames() throws Exception {
//        String homePath = "/Users/tanner/Documents/yonyou/nchomes/ncc2111";
//        ClassLoader classLoader = ClassLoaderUtil.getUapJdbcClassLoader(homePath);
//        String driverClass = "oracle.jdbc.driver.OracleDriver";
//        String userName = "ncc2111";
//        String pwd = "1";
//        String jdbcUrl = "jdbc:oracle:thin:@127.0.0.1:1521:orcl";
//        Class<?> driverClazz = classLoader.loadClass(driverClass);
//        Driver deiver = (Driver) driverClazz.getConstructor().newInstance();
//        Properties properties = new Properties();
//        properties.put("user", userName);
//        properties.put("password", pwd);
//        Connection connection = deiver.connect(jdbcUrl, properties);
//        String querySql = "select * from SIM_STOCK_IDISTILL";
//        PreparedStatement preparedStatement = connection.prepareStatement(querySql);
//        ResultSet resultSet = preparedStatement.executeQuery();
//        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
//        for (int i = 1; i < resultSetMetaData.getColumnCount(); i++) {
//            System.out.println(resultSetMetaData.getColumnName(i));
//        }
//    }

}

