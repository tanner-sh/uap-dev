package com.tanner.patcher.action;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OpenApiPathTest {

    @Test
    public void onlyMarkdownInsideOpenApiDirectoryIsExportable() {
        assertTrue(ExportPatcherUtil.isOpenApiMarkdown(
                "/project/hotwebs/nccloud/openapi/demo.md"));
        assertTrue(ExportPatcherUtil.isOpenApiMarkdown(
                "C:\\project\\openapi\\nested\\demo.md"));
        assertFalse(ExportPatcherUtil.isOpenApiMarkdown(
                "/project/src/main/resources/README.md"));
        assertFalse(ExportPatcherUtil.isOpenApiMarkdown(
                "/project/openapi/demo.txt"));
    }
}
