package com.tanner.debug.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CreatApplicationConfigurationUtilTest {

    @Test
    public void quotesVmPropertyValuesContainingSpaces() {
        StringBuilder parameters = new StringBuilder();
        CreatApplicationConfigurationUtil.appendVmProperty(parameters,
                "nc.server.location", "/tmp/NC Home");
        assertEquals("-Dnc.server.location=\"/tmp/NC Home\"\n", parameters.toString());
    }
}
