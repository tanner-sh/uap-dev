package com.tanner;

import com.tanner.devconfig.util.AESEncode;

public class EncodeDecodeTest {

    @org.junit.Test
    public void encode() throws Exception {
        System.out.println(AESEncode.encrypt("1", "D:\\yonyou\\nchome\\ncc2111"));
    }

    @org.junit.Test
    public void decode() throws Exception {
        System.out.println(AESEncode.decrypt("#9B8A8D3C81B33E7E1DD5F10BF93CFDCF", "D:\\yonyou\\nchome\\ncc2111"));
    }

}
