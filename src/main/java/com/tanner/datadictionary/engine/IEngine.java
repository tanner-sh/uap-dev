package com.tanner.datadictionary.engine;

import com.intellij.openapi.progress.ProgressIndicator;
import com.tanner.base.BusinessException;
import com.tanner.datadictionary.entity.ColumnInfo;
import com.tanner.datadictionary.entity.TableInfo;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.util.List;

public interface IEngine {

    default List<TableInfo> getAllTableInfo(Connection connection, String userName,
                                            String[] tableNamePattern)
            throws BusinessException {
        return getAllTableInfo(connection, userName, tableNamePattern, null);
    }

    List<TableInfo> getAllTableInfo(Connection connection, String userName,
                                    String[] tableNamePattern,
                                    @Nullable ProgressIndicator indicator)
            throws BusinessException;

    default List<ColumnInfo> getAllColumnInfo(Connection connection, String tableName,
                                              boolean needFilterDefField)
            throws BusinessException {
        return getAllColumnInfo(connection, tableName, needFilterDefField, null);
    }

    List<ColumnInfo> getAllColumnInfo(Connection connection, String tableName,
                                      boolean needFilterDefField,
                                      @Nullable ProgressIndicator indicator)
            throws BusinessException;

}
