package com.tanner;

import com.tanner.base.UapUtil;

import java.io.File;

public class UapVersionTest {

    @org.junit.Test
    public void getUapVersion() throws Exception {
        String[] homes = new String[]{
                "D:\\yonyou\\nchome\\nchome57",
                "D:\\yonyou\\nchome\\nchome_dw616\\nchome_dw",
                "D:\\yonyou\\nchome\\nchomencc1909_htzq",
                "D:\\yonyou\\nchome\\ncchome_ncc2005",
                "D:\\yonyou\\nchome\\NCC2105-minihome",
                "D:\\yonyou\\nchome\\ncc2111",
        };
        for (String home : homes) {
            System.out.println(UapUtil.getUapVersion(home));
        }
        for (String home : homes) {
            File propFile = new File(home, "/ierp/bin/key.properties");
            if (propFile.exists()) {
                System.out.println(home);
            }
        }
    }

}
