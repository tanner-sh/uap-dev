package com.tanner.prop.entity;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ToolUtilsTest {

    @Test
    public void parsesAndRebuildsCommonJdbcUrlsWithoutLosingSuffixes() {
        assertJdbcRoundTrip(
                "jdbc:mysql://old:3306/demo?useUnicode=true",
                "jdbc:mysql://new-host:3307/newdb?useUnicode=true");
        assertJdbcRoundTrip(
                "jdbc:sqlserver://old:1433;databaseName=demo;encrypt=true",
                "jdbc:sqlserver://new-host:3307;databaseName=newdb;encrypt=true");
        assertJdbcRoundTrip(
                "jdbc:oracle:thin:@old:1521:orcl",
                "jdbc:oracle:thin:@new-host:3307:newdb");
        assertJdbcRoundTrip(
                "jdbc:oracle:thin:@//old:1521/orclpdb",
                "jdbc:oracle:thin:@//new-host:3307/newdb");
    }

    @Test
    public void parsesAndRebuildsOracleDescriptorUrl() {
        String original = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)"
                + "(HOST=old)(PORT=1521))(CONNECT_DATA=(SERVICE_NAME=orclpdb)))";
        assertArrayEquals(new String[]{"old", "1521", "orclpdb"},
                ToolUtils.getJDBCInfo(original));
        assertEquals("jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)"
                        + "(HOST=new-host)(PORT=3307))(CONNECT_DATA=(SERVICE_NAME=newdb)))",
                ToolUtils.getJDBCUrl(original, "newdb", "new-host", "3307"));
    }

    @Test
    public void identifiesJdbcUrlsCaseInsensitivelyAndExcludesOdbc() {
        assertTrue(ToolUtils.isJDBCUrl("JDBC:mysql://localhost/demo"));
        assertFalse(ToolUtils.isJDBCUrl("jdbc:odbc:demo"));
        assertFalse(ToolUtils.isJDBCUrl(null));
    }

    private void assertJdbcRoundTrip(String original, String rebuilt) {
        assertArrayEquals(new String[]{"old", original.contains("1433") ? "1433"
                        : original.contains("1521") ? "1521" : "3306",
                        original.contains("orclpdb") ? "orclpdb"
                                : original.contains("orcl") ? "orcl" : "demo"},
                ToolUtils.getJDBCInfo(original));
        assertEquals(rebuilt,
                ToolUtils.getJDBCUrl(original, "newdb", "new-host", "3307"));
    }
}
