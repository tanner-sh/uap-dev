package com.tanner.datadictionary.engine;

import com.tanner.base.BusinessException;
import com.tanner.datadictionary.entity.ColumnInfo;
import com.tanner.datadictionary.entity.TableInfo;

import java.sql.Connection;
import java.util.List;

public class MySqlEngine implements IEngine {

    @Override
    public List<TableInfo> getAllTableInfo(Connection connection, String userName, String[] tableNamePattern) {
        return null;
    }

    @Override
    public List<ColumnInfo> getAllColumnInfo(Connection connection, String tableName, boolean needFilterDefField) {
        return null;
    }

}
