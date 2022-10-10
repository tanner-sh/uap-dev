package com.tanner;

import com.tanner.base.UapUtil;
import com.tanner.devconfig.util.AESEncode;

import java.io.File;


public class Test {

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

    @org.junit.Test
    public void encode() throws Exception {
        System.out.println(AESEncode.encrypt("1", "D:\\yonyou\\nchome\\ncc2111"));
    }

    @org.junit.Test
    public void decode() throws Exception {
        System.out.println(AESEncode.decrypt("#9B8A8D3C81B33E7E1DD5F10BF93CFDCF", "D:\\yonyou\\nchome\\ncc2111"));
    }

}

