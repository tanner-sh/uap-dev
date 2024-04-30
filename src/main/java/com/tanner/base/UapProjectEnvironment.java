package com.tanner.base;


import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.apache.commons.lang3.StringUtils;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@State(name = "NccEnvSetting", storages = {
        @com.intellij.openapi.components.Storage(value = "$PROJECT_CONFIG_DIR$/uapEnvSetting.xml")})
public class UapProjectEnvironment implements PersistentStateComponent<Element> {

    private final String ATTR_UAPHOME_PATH = "uapHomePath";
    private final String ATTR_UAPVERSION = "uapVersion";
    private final String ATTR__EX_MODULES = "ex_modules";
    private final String ATTR_MUST_MODULES = "must_modules";
    private final String ATTR_CONNECTD_DATA_SOURCE = "connectd_data_source";
    private final String ATTR_NCCLOUDJAR = "nccloudJar";
    private final String ATTR_NCCHRJAR = "ncchrJar";
    private final String ATTR_LASTPATCHERPATH = "lastPatcherPath";
    private final String ATTR_DEVELOPER = "developer";
    private String uapHomePath;//uapHome地址
    private String uapVersion;//uap版本
    private String ex_modules;//忽略的模块
    private String must_modules;//必选模块
    private String connected_data_source;//当前连接的数据源
    private String nccloudJar;
    private String ncchrJAR;
    private String lastPatcherPath;//上一次的补丁地址
    private String developer;// 开发者


    public UapProjectEnvironment() {
    }

    public static UapProjectEnvironment getInstance() {
        Project pro = ProjectManager.getInstance().getProject();
        return getInstance(pro);
    }

    public static UapProjectEnvironment getInstance(Project project) {
        if (project == null) {
            Messages.showMessageDialog("Please open a project", "Error", Messages.getErrorIcon());
            return null;
        } else {
            return ServiceManager.getService(project, UapProjectEnvironment.class);
        }
    }

    @Nullable
    @Override
    public Element getState() {
        Element element = new Element("NccEnvSetting");
        element.setAttribute(ATTR_UAPHOME_PATH, getUapHomePath());
        element.setAttribute(ATTR_UAPVERSION, getUapVersion());
        element.setAttribute(ATTR__EX_MODULES, getEx_modules());
        element.setAttribute(ATTR_MUST_MODULES, getMust_modules());
        element.setAttribute(ATTR_CONNECTD_DATA_SOURCE, getConnected_data_source());
        element.setAttribute(ATTR_NCCLOUDJAR, getNccloudJar());
        element.setAttribute(ATTR_NCCHRJAR, getNcchrJAR());
        element.setAttribute(ATTR_LASTPATCHERPATH, getLastPatcherPath());
        element.setAttribute(ATTR_DEVELOPER, getDeveloper());
        return element;
    }


    @Override
    public void loadState(@NotNull Element element) {
        this.setUapHomePath(element.getAttributeValue(ATTR_UAPHOME_PATH) == null ? ""
                : element.getAttributeValue(ATTR_UAPHOME_PATH));
        this.setUapVersion(element.getAttributeValue(ATTR_UAPVERSION) == null ? ""
                : element.getAttributeValue(ATTR_UAPVERSION));
        this.setEx_modules(element.getAttributeValue(ATTR__EX_MODULES) == null ? ""
                : element.getAttributeValue(ATTR__EX_MODULES));
        this.setMust_modules(element.getAttributeValue(ATTR_MUST_MODULES) == null ? ""
                : element.getAttributeValue(ATTR_MUST_MODULES));
        this.setConnected_data_source(element.getAttributeValue(ATTR_CONNECTD_DATA_SOURCE) == null ? ""
                : element.getAttributeValue(ATTR_CONNECTD_DATA_SOURCE));
        this.setNccloudJar(element.getAttributeValue(ATTR_NCCLOUDJAR) == null ? ""
                : element.getAttributeValue(ATTR_NCCLOUDJAR));
        this.setNcchrJAR(element.getAttributeValue(ATTR_NCCHRJAR) == null ? ""
                : element.getAttributeValue(ATTR_NCCHRJAR));
        this.setLastPatcherPath(element.getAttributeValue(ATTR_LASTPATCHERPATH) == null ? ""
                : element.getAttributeValue(ATTR_LASTPATCHERPATH));
        this.setDeveloper(element.getAttributeValue(ATTR_DEVELOPER) == null ? ""
                : element.getAttributeValue(ATTR_DEVELOPER));
    }

    public String getUapHomePath() {
        return StringUtils.isBlank(uapHomePath) ? "" : uapHomePath;
    }

    public void setUapHomePath(String uapHomePath) {
        this.uapHomePath = uapHomePath;
    }

    public String getEx_modules() {
        return StringUtils.isBlank(ex_modules) ? "" : ex_modules;
    }

    public void setEx_modules(String ex_modules) {
        this.ex_modules = ex_modules;
    }

    public String getMust_modules() {
        return StringUtils.isBlank(must_modules) ? "" : must_modules;
    }

    public void setMust_modules(String must_modules) {
        this.must_modules = must_modules;
    }

    public String getConnected_data_source() {
        return StringUtils.isBlank(connected_data_source) ? "" : connected_data_source;
    }

    public void setConnected_data_source(String connected_data_source) {
        this.connected_data_source = connected_data_source;
    }

    public String getNccloudJar() {
        return StringUtils.isBlank(nccloudJar) ? "" : nccloudJar;
    }

    public void setNccloudJar(String nccloudJar) {
        this.nccloudJar = nccloudJar;
    }

    public String getNcchrJAR() {
        return StringUtils.isBlank(ncchrJAR) ? "" : ncchrJAR;
    }

    public void setNcchrJAR(String ncchrJAR) {
        this.ncchrJAR = ncchrJAR;
    }

    public String getLastPatcherPath() {
        return StringUtils.isBlank(lastPatcherPath) ? "" : lastPatcherPath;
    }

    public void setLastPatcherPath(String lastPatcherPath) {
        this.lastPatcherPath = lastPatcherPath;
    }

    public String getDeveloper() {
        return StringUtils.isBlank(developer) ? "" : developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }

    public String getUapVersion() {
        return StringUtils.isBlank(uapVersion) ? "" : uapVersion;
    }

    public void setUapVersion(String uapVersion) {
        this.uapVersion = uapVersion;
    }
}
