package com.tanner.script.export.util;

import com.intellij.openapi.progress.ProgressIndicator;
import com.tanner.base.ClassLoaderUtil;
import com.tanner.base.DbUtil;
import com.tanner.base.UapUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.InputStream;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


public class ScriptExportTool {

    private final static int EXPORTMODE_DELETE_THEN_INSERT = 0;
    private final static int EXPORTMODE_INSERT = 1;
    private final static int EXPORTMODE_DELETE = 2;
    private String driverClass;
    private String jdbcUrl;
    private String userName;
    private String pwd;
    private String homePath;
    private int exportMode;
    private boolean spiltGo;
    private Connection connection;
    private ProgressIndicator progressIndicator;

    public ScriptExportTool() {

    }

    public ScriptExportTool(String homePath, String driverClass, String jdbcUrl, String userName,
                            String pwd, int exportMode, boolean spiltGo) {
        this.homePath = homePath;
        this.driverClass = driverClass;
        this.jdbcUrl = jdbcUrl;
        this.userName = userName;
        this.pwd = pwd;
        this.exportMode = exportMode;
        this.spiltGo = spiltGo;
    }

    public void export(String exportPath, String heavyNodeCode, String lightNodeCode, String mdName,
                       String mdModule) throws Exception {
        export(exportPath, heavyNodeCode, lightNodeCode, mdName, mdModule, null);
    }

    public void export(String exportPath, String heavyNodeCode, String lightNodeCode, String mdName,
                       String mdModule, ProgressIndicator indicator) throws Exception {
        this.progressIndicator = indicator;
        try (URLClassLoader classLoader = ClassLoaderUtil.getUapJdbcClassLoader(homePath);
             Connection openedConnection = DbUtil.getConnection(classLoader, driverClass, jdbcUrl,
                     userName, pwd)) {
            this.connection = openedConnection;
            checkCanceled();
            exportHeavyNodeCode(exportPath, heavyNodeCode);
            checkCanceled();
            exportLightNodeCode(exportPath, lightNodeCode);
            checkCanceled();
            exportMdName(exportPath, mdName);
            checkCanceled();
            exportMdModule(exportPath, mdModule);
        } finally {
            connection = null;
            progressIndicator = null;
        }
    }

    private List<String> getExportSqls(List<Map<String, String>> configList, String parma) throws Exception {
        List<String> exportSqls = new ArrayList<>();
        switch (exportMode) {
            case EXPORTMODE_DELETE_THEN_INSERT:
                exportSqls.addAll(buildDeleteSqls(configList, parma));
                exportSqls.addAll(buildInsertSqls(configList, parma));
                break;
            case EXPORTMODE_INSERT:
                exportSqls.addAll(buildInsertSqls(configList, parma));
                break;
            case EXPORTMODE_DELETE:
                exportSqls.addAll(buildDeleteSqls(configList, parma));
                break;
            default:
                break;
        }
        return exportSqls;
    }

    private List<String> buildDeleteSqls(List<Map<String, String>> configList, String parma) throws Exception {
        List<String> exportSqls = new ArrayList<>();
        for (Map<String, String> stringStringMap : configList) {
            checkCanceled();
            exportSqls.add(buildDeleteStatement(stringStringMap.get("sql"), parma, spiltGo));
        }
        return exportSqls;
    }

    static String buildDeleteStatement(String querySql, String parameter, boolean splitGo)
            throws Exception {
        if (StringUtils.isBlank(querySql)) {
            throw new IllegalArgumentException("导出配置中的 SQL 不能为空");
        }
        int fromIndex = findSqlKeyword(querySql, "from");
        if (fromIndex < 0) {
            throw new IllegalArgumentException("导出配置 SQL 缺少 FROM: " + querySql);
        }
        String escapedParameter = "'" + Objects.toString(parameter, "")
                .replace("'", "''") + "'";
        String deleteSql = "delete " + replaceSqlPlaceholders(
                querySql.substring(fromIndex), escapedParameter) + ";";
        return splitGo ? deleteSql + "\ngo\n" : deleteSql;
    }

    private static String replaceSqlPlaceholders(String sql, String replacement) {
        StringBuilder result = new StringBuilder(sql.length());
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        for (int i = 0; i < sql.length(); i++) {
            char current = sql.charAt(i);
            if (current == '\'' && !inDoubleQuote) {
                result.append(current);
                if (inSingleQuote && i + 1 < sql.length() && sql.charAt(i + 1) == '\'') {
                    result.append(sql.charAt(++i));
                } else {
                    inSingleQuote = !inSingleQuote;
                }
            } else if (current == '"' && !inSingleQuote) {
                result.append(current);
                inDoubleQuote = !inDoubleQuote;
            } else if (current == '?' && !inSingleQuote && !inDoubleQuote) {
                result.append(replacement);
            } else {
                result.append(current);
            }
        }
        return result.toString();
    }

    private static int findSqlKeyword(String sql, String keyword) {
        String normalizedKeyword = keyword.toLowerCase(Locale.ROOT);
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        for (int i = 0; i <= sql.length() - keyword.length(); i++) {
            char current = sql.charAt(i);
            if (current == '\'' && !inDoubleQuote) {
                if (inSingleQuote && i + 1 < sql.length() && sql.charAt(i + 1) == '\'') {
                    i++;
                    continue;
                }
                inSingleQuote = !inSingleQuote;
                continue;
            }
            if (current == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
                continue;
            }
            if (inSingleQuote || inDoubleQuote) {
                continue;
            }
            int end = i + keyword.length();
            boolean startsAtBoundary = i == 0 || !Character.isJavaIdentifierPart(sql.charAt(i - 1));
            boolean endsAtBoundary = end == sql.length()
                    || !Character.isJavaIdentifierPart(sql.charAt(end));
            if (startsAtBoundary && endsAtBoundary
                    && sql.substring(i, end).toLowerCase(Locale.ROOT)
                    .equals(normalizedKeyword)) {
                return i;
            }
        }
        return -1;
    }

    private List<String> buildInsertSqls(List<Map<String, String>> configList, String parma) throws Exception {
        List<String> exportSqls = new ArrayList<>();
        for (Map<String, String> stringStringMap : configList) {
            checkCanceled();
            String tableName = stringStringMap.get("tableName");
            String querySql = stringStringMap.get("sql");
            int count = countSqlPlaceholders(querySql);
            List<Object> paramList = new ArrayList<>(Collections.nCopies(count, parma));
            exportSqls.addAll(DbUtil.getInsertScripts(connection, tableName, querySql, paramList,
                    spiltGo, progressIndicator));
        }
        return exportSqls;
    }

    private List<Object> getAllHeavyNodeCodeByParent(String heavyNodeCode) throws Exception {
        LinkedHashSet<Object> heavyCodes = new LinkedHashSet<>();
        if (isModuleId(heavyNodeCode)) {
            for (String moduleId : getDescendantModuleIds(heavyNodeCode)) {
                checkCanceled();
                List<Map<String, Object>> rows = DbUtil.executeQuery(connection,
                        "select FUNCODE from SM_FUNCREGISTER where OWN_MODULE = ?",
                        Collections.singletonList(moduleId));
                rows.forEach(row -> addValue(row, "FUNCODE", heavyCodes));
            }
        } else {
            collectNodeCodes("SM_FUNCREGISTER", "FUNCODE", "CFUNID", "PARENT_ID",
                    heavyNodeCode, heavyCodes);
        }
        return new ArrayList<>(heavyCodes);
    }

    private List<String> getDescendantModuleIds(String rootModuleId) throws Exception {
        LinkedHashSet<String> moduleIds = new LinkedHashSet<>();
        Deque<String> pending = new ArrayDeque<>();
        pending.add(rootModuleId);
        while (!pending.isEmpty()) {
            checkCanceled();
            String moduleId = pending.removeFirst();
            if (!moduleIds.add(moduleId)) {
                continue;
            }
            List<Map<String, Object>> rows = DbUtil.executeQuery(connection,
                    "select MODULEID from DAP_DAPSYSTEM where PARENTCODE = ?",
                    Collections.singletonList(moduleId));
            for (Map<String, Object> row : rows) {
                String child = Objects.toString(row.get("MODULEID"), "");
                if (!child.isBlank() && !moduleIds.contains(child)) {
                    pending.addLast(child);
                }
            }
        }
        return new ArrayList<>(moduleIds);
    }

    private void collectNodeCodes(String tableName, String codeColumn, String idColumn,
                                  String parentColumn, String rootCode,
                                  LinkedHashSet<Object> codes) throws Exception {
        checkCanceled();
        List<Map<String, Object>> roots = DbUtil.executeQuery(connection,
                "select " + idColumn + "," + codeColumn + " from " + tableName
                        + " where " + codeColumn + " = ?",
                Collections.singletonList(rootCode));
        Deque<String> pending = new ArrayDeque<>();
        Set<String> visited = new LinkedHashSet<>();
        for (Map<String, Object> root : roots) {
            addValue(root, codeColumn, codes);
            String rootId = Objects.toString(root.get(idColumn), "");
            if (!rootId.isBlank()) {
                pending.add(rootId);
            }
        }
        while (!pending.isEmpty()) {
            checkCanceled();
            String parentId = pending.removeFirst();
            if (!visited.add(parentId)) {
                continue;
            }
            List<Map<String, Object>> children = DbUtil.executeQuery(connection,
                    "select " + idColumn + "," + codeColumn + " from " + tableName
                            + " where " + parentColumn + " = ? order by " + codeColumn,
                    Collections.singletonList(parentId));
            for (Map<String, Object> child : children) {
                addValue(child, codeColumn, codes);
                String childId = Objects.toString(child.get(idColumn), "");
                if (!childId.isBlank() && !visited.contains(childId)) {
                    pending.addLast(childId);
                }
            }
        }
    }

    private void addValue(Map<String, Object> row, String key, Set<Object> values) {
        Object value = row.get(key);
        if (value != null) {
            values.add(value);
        }
    }

    private List<Map<String, String>> readExportConfig(String yamlName) throws Exception {
        Yaml yaml = new Yaml();
        String yamlPath = "../../../../../config/" + yamlName;
        try (InputStream resourceAsStream = this.getClass().getResourceAsStream(yamlPath)) {
            if (resourceAsStream == null) {
                throw new IllegalArgumentException("找不到导出配置: " + yamlName);
            }
            return yaml.load(resourceAsStream);
        }
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
            checkCanceled();
            File scriptFile = resolveSqlFile(scriptDirectory, Objects.toString(nodeCode, ""));
            scriptFile.createNewFile();
            writeSqlLines(scriptFile, getExportSqls(configList,
                    Objects.toString(nodeCode, "")));
        }
    }

    private List<Object> getAllLightNodeCodeByParent(String lightNodeCode) throws Exception {
        LinkedHashSet<Object> lightCodes = new LinkedHashSet<>();
        if (isModuleId(lightNodeCode)) {
            for (String moduleId : getDescendantModuleIds(lightNodeCode)) {
                checkCanceled();
                List<Map<String, Object>> rows = DbUtil.executeQuery(connection,
                        "select CODE from SM_APPREGISTER where OWN_MODULE = ?",
                        Collections.singletonList(moduleId));
                rows.forEach(row -> addValue(row, "CODE", lightCodes));
            }
        } else {
            collectNodeCodes("SM_APPREGISTER", "CODE", "PK_APPREGISTER", "PARENT_ID",
                    lightNodeCode, lightCodes);
        }
        return new ArrayList<>(lightCodes);
    }

    private boolean isModuleId(String code) throws Exception {
        String sql = "select 1 from DAP_DAPSYSTEM where MODULEID = ?";
        List<Map<String, Object>> list = DbUtil.executeQuery(connection, sql, Collections.singletonList(code));
        return CollectionUtils.isNotEmpty(list);
    }

    private void exportLightNodeCode(String exportPath, String lightNodeCode) throws Exception {
        if (StringUtils.isEmpty(lightNodeCode)) {
            return;
        }
        File scriptDirectory = new File(exportPath, "lightNodeCode");
        if (!scriptDirectory.exists()) {
            scriptDirectory.mkdirs();
        }
        String version = UapUtil.getUapVersion(homePath);
        List<Map<String, String>> configList = readExportConfig(selectLightNodeConfig(version));
        List<Object> allLightNodeCodeByParent = getAllLightNodeCodeByParent(lightNodeCode);
        for (Object nodeCode : allLightNodeCodeByParent) {
            checkCanceled();
            File scriptFile = resolveSqlFile(scriptDirectory, Objects.toString(nodeCode, ""));
            scriptFile.createNewFile();
            writeSqlLines(scriptFile, getExportSqls(configList,
                    Objects.toString(nodeCode, "")));
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
        File scriptFile = resolveSqlFile(scriptDirectory, mdName);
        scriptFile.createNewFile();
        writeSqlLines(scriptFile, getExportSqls(configList, mdName));
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
        File scriptFile = resolveSqlFile(scriptDirectory, mdModule);
        scriptFile.createNewFile();
        writeSqlLines(scriptFile, getExportSqls(configList, mdModule));
    }

    static String selectLightNodeConfig(String version) {
        if (version != null && version.startsWith("ncc")) {
            String digits = version.substring(3).replaceAll("\\D", "");
            if (digits.length() >= 4) {
                try {
                    if (Integer.parseInt(digits.substring(0, 4)) >= 2005) {
                        return "lightNodeCode_ncc2005.yaml";
                    }
                } catch (NumberFormatException ignored) {
                    // Use the compatible generic configuration.
                }
            }
        }
        return "lightNodeCode.yaml";
    }

    static int countSqlPlaceholders(String sql) {
        int count = 0;
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        for (int i = 0; i < sql.length(); i++) {
            char current = sql.charAt(i);
            if (current == '\'' && !inDoubleQuote) {
                if (inSingleQuote && i + 1 < sql.length() && sql.charAt(i + 1) == '\'') {
                    i++;
                } else {
                    inSingleQuote = !inSingleQuote;
                }
            } else if (current == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
            } else if (current == '?' && !inSingleQuote && !inDoubleQuote) {
                count++;
            }
        }
        return count;
    }

    private void checkCanceled() {
        if (progressIndicator != null) {
            progressIndicator.checkCanceled();
        }
    }

    private File resolveSqlFile(File directory, String name) throws Exception {
        if (StringUtils.isBlank(name) || !name.matches("[\\p{L}\\p{N}._-]+")
                || ".".equals(name) || "..".equals(name)) {
            throw new IllegalArgumentException("非法导出文件名: " + name);
        }
        Path directoryPath = directory.toPath().toAbsolutePath().normalize();
        Path filePath = directoryPath.resolve(name + ".sql").normalize();
        if (!filePath.getParent().equals(directoryPath)) {
            throw new IllegalArgumentException("非法导出文件名: " + name);
        }
        return filePath.toFile();
    }

    private void writeSqlLines(File file, List<String> lines) throws Exception {
        FileUtils.writeLines(file, StandardCharsets.UTF_8.name(), lines,
                System.lineSeparator());
    }

}
