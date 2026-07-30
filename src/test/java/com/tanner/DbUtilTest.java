package com.tanner;

import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.tanner.base.BusinessException;
import com.tanner.base.DbUtil;
import com.tanner.datadictionary.engine.MySqlEngine;
import com.tanner.datadictionary.engine.OracleEngine;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Types;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialClob;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DbUtilTest {

    @Test
    public void selectsDatabaseEngineCaseInsensitively() throws Exception {
        assertTrue(DbUtil.getEngine(connectionFor("MySQL")) instanceof MySqlEngine);
        assertTrue(DbUtil.getEngine(connectionFor("OceanBase")) instanceof MySqlEngine);
        assertTrue(DbUtil.getEngine(connectionFor("Oracle")) instanceof OracleEngine);
    }

    @Test
    public void rejectsUnsupportedDatabase() throws Exception {
        try {
            DbUtil.getEngine(connectionFor("PostgreSQL"));
            fail("Unsupported database should fail");
        } catch (BusinessException expected) {
            assertTrue(expected.getMessage().contains("PostgreSQL"));
        }
    }

    @Test
    public void formatsSqlLiteralsSafely() {
        assertEquals("NULL", DbUtil.getColumnValue(Types.VARCHAR, null));
        assertEquals("'O''Reilly'", DbUtil.getColumnValue(Types.VARCHAR, "O'Reilly"));
        assertEquals("'2026-07-30'", DbUtil.getColumnValue(Types.DATE, "2026-07-30"));
        assertEquals("1", DbUtil.getColumnValue(Types.BOOLEAN, true));
        assertEquals("X'012a'", DbUtil.getColumnValue(Types.VARBINARY,
                new byte[]{0x01, 0x2a}));
        assertEquals("42", DbUtil.getColumnValue(Types.INTEGER, 42));
    }

    @Test
    public void readsLobContentsInsteadOfJdbcObjectNames() throws Exception {
        assertEquals("'大字段''内容'", DbUtil.getColumnValue(Types.CLOB,
                new SerialClob("大字段'内容".toCharArray())));
        assertEquals("X'012a'", DbUtil.getColumnValue(Types.BLOB,
                new SerialBlob(new byte[]{0x01, 0x2a})));
    }

    @Test
    public void appliesQueryTimeoutAndPreservesSqlCause() {
        AtomicInteger timeout = new AtomicInteger();
        SQLException sqlFailure = new SQLException("database unavailable");
        PreparedStatement statement = (PreparedStatement) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class[]{PreparedStatement.class},
                (proxy, method, args) -> {
                    if ("setQueryTimeout".equals(method.getName())) {
                        timeout.set((Integer) args[0]);
                        return null;
                    }
                    if ("executeQuery".equals(method.getName())) {
                        throw sqlFailure;
                    }
                    return defaultValue(method.getReturnType());
                });
        Connection connection = (Connection) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class[]{Connection.class},
                (proxy, method, args) -> {
                    if ("prepareStatement".equals(method.getName())) {
                        return statement;
                    }
                    return defaultValue(method.getReturnType());
                });

        try {
            DbUtil.executeQuery(connection, "select 1", List.of());
            fail("SQL failure should be propagated");
        } catch (BusinessException expected) {
            assertEquals(DbUtil.QUERY_TIMEOUT_SECONDS, timeout.get());
            assertEquals(sqlFailure, expected.getCause());
        }
    }

    @Test(expected = ProcessCanceledException.class)
    public void rejectsCanceledQueryBeforePreparingStatement() throws Exception {
        ProgressIndicator indicator = (ProgressIndicator) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class[]{ProgressIndicator.class},
                (proxy, method, args) -> {
                    if ("checkCanceled".equals(method.getName())) {
                        throw new ProcessCanceledException();
                    }
                    return defaultValue(method.getReturnType());
                });
        DbUtil.executeQuery(connectionFor("MySQL"), "select 1", List.of(), indicator);
    }

    private Connection connectionFor(String productName) {
        DatabaseMetaData metadata = (DatabaseMetaData) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class[]{DatabaseMetaData.class},
                (proxy, method, args) -> {
                    if ("getDatabaseProductName".equals(method.getName())) {
                        return productName;
                    }
                    return defaultValue(method.getReturnType());
                });
        return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class[]{Connection.class}, (proxy, method, args) -> {
                    if ("getMetaData".equals(method.getName())) {
                        return metadata;
                    }
                    return defaultValue(method.getReturnType());
                });
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
