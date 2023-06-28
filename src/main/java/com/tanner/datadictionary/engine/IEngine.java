package com.tanner.datadictionary.engine;

import com.tanner.base.BusinessException;
import com.tanner.datadictionary.entity.ColumnInfo;
import com.tanner.datadictionary.entity.TableInfo;

import java.sql.Connection;
import java.util.List;

public interface IEngine {

    List<TableInfo> getAllTableInfo(Connection connection,
                                    String userName,
                                    String[] tableNamePattern)
            throws BusinessException;

    List<ColumnInfo> getAllColumnInfo(Connection connection,
                                      String tableName,
                                      boolean needFilterDefField)
            throws BusinessException;

}
