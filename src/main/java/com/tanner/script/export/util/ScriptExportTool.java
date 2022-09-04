package com.tanner.script.export.util;

import com.tanner.base.ClassLoaderUtil;
import com.tanner.base.DbUtil;
import com.tanner.base.UapProjectEnvironment;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.yaml.snakeyaml.Yaml;


public class ScriptExportTool {

  private String driverClass;
  private String jdbcUrl;
  private String userName;
  private String pwd;
  private Connection connection;

  public ScriptExportTool() {

  }

  public ScriptExportTool(String driverClass, String jdbcUrl, String userName, String pwd) {
    this.driverClass = driverClass;
    this.jdbcUrl = jdbcUrl;
    this.userName = userName;
    this.pwd = pwd;
  }

  public void export(String exportPath, String heavyNodeCode, String lightNodeCode, String mdName,
      String mdModule) throws Exception {
    String homePath = UapProjectEnvironment.getInstance().getUapHomePath();
    ClassLoader classLoader = ClassLoaderUtil.getUapJdbcClassLoader(homePath);
    this.connection = DbUtil.getConnection(classLoader, driverClass, jdbcUrl, userName, pwd);
    exportHeavyNodeCode(exportPath, heavyNodeCode);
    exportLightNodeCode(exportPath, lightNodeCode);
    exportMdName(exportPath, mdName);
    exportMdModule(exportPath, mdModule);
    DbUtil.closeResource(connection, null, null);
  }

  private List<String> getExportSqls(List<Map<String, String>> configList, String parma)
      throws Exception {
    List<String> exportSqls = new ArrayList<String>();
    for (Map<String, String> stringStringMap : configList) {
      String deleteSql = stringStringMap.get("sql");
      deleteSql = deleteSql.replaceAll("\\?", "'" + parma + "'");
      deleteSql = deleteSql.replaceFirst("select \\*", "delete");
      deleteSql += ";";
      exportSqls.add(deleteSql);
    }
    for (Map<String, String> stringStringMap : configList) {
      String tableName = stringStringMap.get("tableName");
      String querySql = stringStringMap.get("sql");
      long count = querySql.codePoints().filter(ch -> ch == '?').count();
      List<Object> paramList = Arrays.stream(new Object[(int) count]).map(p -> p = parma)
          .collect(Collectors.toList());
      exportSqls.addAll(DbUtil.getInsertScripts(connection, tableName, querySql, paramList));
    }
    return exportSqls;
  }

  private List<Object> getAllHeavyNodeCodeByParent(String heavyNodeCode) throws Exception {
    List<Object> nodeCodes = new ArrayList<Object>();
    StringBuilder querySql = new StringBuilder("select FUNCODE from SM_FUNCREGISTER");
    querySql.append(" start with CFUNID = (select CFUNID from SM_FUNCREGISTER");
    querySql.append(" where FUNCODE = ?) CONNECT BY PRIOR CFUNID = PARENT_ID order by FUNCODE");
    List<Map<String, Object>> list = DbUtil.executeQuery(connection, querySql.toString(),
        Collections.singletonList(heavyNodeCode));
    list.forEach(mm -> nodeCodes.addAll(mm.values()));
    return nodeCodes;
  }

  private List<Map<String, String>> readExportConfig(String yamlName) {
    Yaml yaml = new Yaml();
    String yamlPath = "../../../../../config/" + yamlName;
    InputStream resourceAsStream = this.getClass().getResourceAsStream(yamlPath);
    List<Map<String, String>> list = yaml.load(resourceAsStream);
    return list;
  }

  private void exportHeavyNodeCode(String exportPath, String heavyNodeCode) throws Exception {
    if (StringUtils.isEmpty(heavyNodeCode)) {
      return;
    }
    File scriptDirectory = new File(exportPath, "heavyNodeCode");
    if (!scriptDirectory.exists()) {
      scriptDirectory.mkdirs();
    }
    List<Map<String, String>> configList = readExportConfig("heavyNodeCode.yaml");
    List<Object> allHeavyNodeCodeByParent = getAllHeavyNodeCodeByParent(heavyNodeCode);
    for (Object nodeCode : allHeavyNodeCodeByParent) {
      File scriptFile = new File(scriptDirectory, nodeCode + ".sql");
      scriptFile.deleteOnExit();
      scriptFile.createNewFile();
      FileUtils.writeLines(scriptFile, getExportSqls(configList, (String) nodeCode));
    }
  }

  private List<Object> getAllLightNodeCodeByParent(String lightNodeCode) throws Exception {
    List<Object> nodeCodes = new ArrayList<Object>();
    StringBuilder querySql = new StringBuilder("select code from sm_appregister");
    querySql.append(" start with PK_APPREGISTER = (select PK_APPREGISTER from sm_appregister");
    querySql.append(" where CODE = ?) CONNECT BY PRIOR PK_APPREGISTER = PARENT_ID order by code");
    List<Map<String, Object>> list = DbUtil.executeQuery(connection, querySql.toString(),
        Collections.singletonList(lightNodeCode));
    list.forEach(mm -> nodeCodes.addAll(mm.values()));
    return nodeCodes;
  }

  private void exportLightNodeCode(String exportPath, String lightNodeCode) throws Exception {
    if (StringUtils.isEmpty(lightNodeCode)) {
      return;
    }
    File scriptDirectory = new File(exportPath, "lightNodeCode");
    if (!scriptDirectory.exists()) {
      scriptDirectory.mkdirs();
    }
    List<Map<String, String>> configList = readExportConfig("lightNodeCode.yaml");
    List<Object> allLightNodeCodeByParent = getAllLightNodeCodeByParent(lightNodeCode);
    for (Object nodeCode : allLightNodeCodeByParent) {
      File scriptFile = new File(scriptDirectory, nodeCode + ".sql");
      scriptFile.deleteOnExit();
      scriptFile.createNewFile();
      FileUtils.writeLines(scriptFile, getExportSqls(configList, (String) nodeCode));
    }
  }

  private void exportMdName(String exportPath, String mdName) throws Exception {
    if (StringUtils.isEmpty(mdName)) {
      return;
    }
    File scriptDirectory = new File(exportPath, "mdName");
    if (!scriptDirectory.exists()) {
      scriptDirectory.mkdirs();
    }
    List<Map<String, String>> configList = readExportConfig("mdName.yaml");
    File scriptFile = new File(scriptDirectory, mdName + ".sql");
    scriptFile.deleteOnExit();
    scriptFile.createNewFile();
    FileUtils.writeLines(scriptFile, getExportSqls(configList, mdName));
  }

  private void exportMdModule(String exportPath, String mdModule) throws Exception {
    if (StringUtils.isEmpty(mdModule)) {
      return;
    }
    File scriptDirectory = new File(exportPath, "mdModule");
    if (!scriptDirectory.exists()) {
      scriptDirectory.mkdirs();
    }
    List<Map<String, String>> configList = readExportConfig("mdModule.yaml");
    File scriptFile = new File(scriptDirectory, mdModule + ".sql");
    scriptFile.deleteOnExit();
    scriptFile.createNewFile();
    FileUtils.writeLines(scriptFile, getExportSqls(configList, mdModule));
  }

}
