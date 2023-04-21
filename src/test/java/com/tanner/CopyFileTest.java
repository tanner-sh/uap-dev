package com.tanner;

public class CopyFileTest {

    @org.junit.Test
    public void copyFile() throws Exception {
        String userName = System.getProperties().getProperty("user.name", "unknown");
        System.out.println(userName);
    }

}
