package com.tanner.abs;

import com.intellij.openapi.project.Project;
import com.tanner.dbdriver.entity.DatabaseDriverInfo;
import com.tanner.dbdriver.entity.DriverInfo;
import com.tanner.prop.entity.DataSourceMeta;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractDataSourceDialog extends AbstractDialog {

    //数据源相关缓存
    private final Map<String, DatabaseDriverInfo> databaseDriverInfoMap = new HashMap<>();
    private final Map<String, DataSourceMeta> dataSourceMetaMap = new LinkedHashMap<>();
    private final Map<String, DriverInfo> driverInfoMap = new HashMap<>();
    //当前数据源
    private DataSourceMeta currMeta;

    protected AbstractDataSourceDialog(@Nullable Project project) {
        super(project);
    }

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
