package com.tanner.base;

import java.util.HashSet;
import java.util.Set;

public class ModuleFileUtil {

    /**
     * 必选模块 目前这么定义，具体的有待考究
     *
     * @return Set<String>
     */
    public static Set<String> getMustMoudleSet() {
        Set<String> moduleSet = new HashSet<>();
        //公共
        moduleSet.add("baseapp");
        moduleSet.add("iuap");
        moduleSet.add("opm");
        moduleSet.add("platform");
        moduleSet.add("pubapp");
        moduleSet.add("pubapputil");
        //框架
        moduleSet.add("riaaam");
        moduleSet.add("riaadp");
        moduleSet.add("riaam");
        moduleSet.add("riacc");
        moduleSet.add("riadc");
        moduleSet.add("riamm");
        moduleSet.add("riaorg");
        moduleSet.add("riart");
        moduleSet.add("riasm");
        moduleSet.add("riawf");
        //uap
        moduleSet.add("uapbd");
        moduleSet.add("uapbs");
        moduleSet.add("uapec");
        moduleSet.add("uapfw");
        moduleSet.add("uapfwjca");
        moduleSet.add("uapmw");
        moduleSet.add("uapportal");
        moduleSet.add("uapsc");
        moduleSet.add("uapss");
        //ncc
        moduleSet.add("workbench");
        moduleSet.add("imag");
        moduleSet.add("graphic_report");
        moduleSet.add("sscrp");
        return moduleSet;
    }
}
