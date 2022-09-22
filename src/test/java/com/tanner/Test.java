package com.tanner;

import com.tanner.base.UapUtil;

public class Test {

    @org.junit.Test
    public void getUapVersion() throws Exception {
        String[] homes = new String[]{"D:\\yonyou\\nchomes\\nchome57",
                "D:\\yonyou\\nchomes\\nchome_dw616\\nchome_dw", "D:\\yonyou\\nchomes\\nchomencc1909_htzq",
                "D:\\yonyou\\nchomes\\ncchome_ncc2005_gdzq", "D:\\yonyou\\nchomes\\NCC2105-minihome"};
        for (String home : homes) {
            System.out.println(UapUtil.getUapVersion(home));
        }
    }

}

