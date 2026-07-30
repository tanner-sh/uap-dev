package com.tanner;

import com.tanner.base.BusinessException;
import com.tanner.patcher.action.ExportPatcherUtil;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ExportPatcherValidationTest {

    @Test
    public void acceptsPortableFileNameParts() throws Exception {
        ExportPatcherUtil.validateFileNamePart("补丁_2026-07.30", "name");
    }

    @Test
    public void rejectsSeparatorsAndParentSegments() throws Exception {
        assertInvalid("../patch");
        assertInvalid("a/b");
        assertInvalid("..");
    }

    private void assertInvalid(String value) throws Exception {
        try {
            ExportPatcherUtil.validateFileNamePart(value, "name");
            fail("Expected invalid value: " + value);
        } catch (BusinessException expected) {
            assertTrue(expected.getMessage().contains("name"));
        }
    }
}
