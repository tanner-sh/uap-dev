package com.tanner.datadictionary.tool;

import com.tanner.datadictionary.entity.AggTable;

import java.util.List;

public interface IExportBuilder {

    void build(List<AggTable> aggTableList, String exportDirPath) throws Exception;

}
