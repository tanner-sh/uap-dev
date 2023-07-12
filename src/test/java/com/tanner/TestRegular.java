package com.tanner;

public class TestRegular {

    @org.junit.Test
    public void copyFile() throws Exception {
        String sql = "select * from sm_appparam where PARENTID in (select PK_APPREGISTER from sm_appregister where CODE = ?)";
        sql = "delete " + sql.substring(sql.indexOf("from"));
        System.out.println(sql);
    }

}
