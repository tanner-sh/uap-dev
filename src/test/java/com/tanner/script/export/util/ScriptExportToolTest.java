package com.tanner.script.export.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ScriptExportToolTest {

    @Test
    public void buildsDeleteStatementWithCaseInsensitiveFrom() throws Exception {
        assertEquals("delete FROM demo WHERE code = 'O''Reilly';",
                ScriptExportTool.buildDeleteStatement(
                        "SELECT 'from literal' AS label FROM demo WHERE code = ?",
                        "O'Reilly", false));
        assertEquals("delete FROM demo WHERE note = '?' AND code = 'A';",
                ScriptExportTool.buildDeleteStatement(
                        "SELECT * FROM demo WHERE note = '?' AND code = ?", "A", false));
        assertEquals("delete from demo where code = 'A';\ngo\n",
                ScriptExportTool.buildDeleteStatement(
                        "select * from demo where code = ?", "A", true));
    }

    @Test
    public void rejectsQueryWithoutFromClause() throws Exception {
        try {
            ScriptExportTool.buildDeleteStatement("select 1", "A", false);
            fail("Missing FROM should fail");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("缺少 FROM"));
        }
    }
}
