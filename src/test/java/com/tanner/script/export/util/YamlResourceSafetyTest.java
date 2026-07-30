package com.tanner.script.export.util;

import org.junit.Test;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class YamlResourceSafetyTest {

    @Test
    public void existingExportResourcesRemainStringMaps() {
        for (String resource : List.of(
                "/config/heavyNodeCode.yaml",
                "/config/lightNodeCode.yaml",
                "/config/lightNodeCode_ncc2005.yaml",
                "/config/mdName.yaml",
                "/config/mdModule.yaml")) {
            Object loaded = load(resource);
            assertTrue(resource, loaded instanceof List<?>);
            assertFalse(resource, ((List<?>) loaded).isEmpty());
            for (Object item : (List<?>) loaded) {
                assertTrue(resource, item instanceof Map<?, ?>);
                ((Map<?, ?>) item).forEach((key, value) -> {
                    assertTrue(resource, key instanceof String);
                    assertTrue(resource, value instanceof String);
                });
            }
        }
    }

    @Test
    public void safeConstructorRejectsArbitraryJavaTags() {
        try {
            safeYaml().load("!!java.net.URL [\"https://example.com\"]");
            fail("Arbitrary Java tags must be rejected");
        } catch (Exception expected) {
            assertNotNull(expected.getMessage());
        }
    }

    private Object load(String resource) {
        try (InputStream input = getClass().getResourceAsStream(resource)) {
            assertNotNull(resource, input);
            return safeYaml().load(input);
        } catch (Exception exception) {
            throw new AssertionError(resource, exception);
        }
    }

    private Yaml safeYaml() {
        LoaderOptions options = new LoaderOptions();
        options.setAllowDuplicateKeys(false);
        options.setMaxAliasesForCollections(20);
        return new Yaml(new SafeConstructor(options));
    }
}
