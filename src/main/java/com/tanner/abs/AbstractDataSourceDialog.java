package com.tanner.abs;

import com.tanner.dbdriver.entity.DatabaseDriverInfo;
import com.tanner.dbdriver.entity.DriverInfo;
import com.tanner.prop.entity.DataSourceMeta;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractDataSourceDialog extends AbstractDialog {

    //数据源相关缓存
    private Map<String, DatabaseDriverInfo> databaseDriverInfoMap = new HashMap<String, DatabaseDriverInfo>();
    private Map<String, DataSourceMeta> dataSourceMetaMap = new LinkedHashMap<String, DataSourceMeta>();
    private Map<String, DriverInfo> driverInfoMap = new HashMap<String, DriverInfo>();
    //当前数据源
    private DataSourceMeta currMeta;

    public Map<String, DatabaseDriverInfo> getDatabaseDriverInfoMap() {
        return databaseDriverInfoMap;
    }

    public LinkedHashMap<String, DataSourceMeta> getDataSourceMetaMap() {
        return (LinkedHashMap<String, DataSourceMeta>) dataSourceMetaMap;
    }

    public Map<String, DriverInfo> getDriverInfoMap() {
        return driverInfoMap;
    }

    public DataSourceMeta getCurrMeta() {
        return currMeta;
    }

    public void setCurrMeta(DataSourceMeta currMeta) {
        this.currMeta = currMeta;
    }

}
