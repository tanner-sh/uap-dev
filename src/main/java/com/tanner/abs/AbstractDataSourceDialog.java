package com.tanner.abs;

import com.intellij.openapi.project.Project;
import com.tanner.dbdriver.entity.DatabaseDriverInfo;
import com.tanner.dbdriver.entity.DriverInfo;
import com.tanner.prop.entity.DataSourceMeta;
import org.jetbrains.annotations.Nullable;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractDataSourceDialog extends AbstractDialog {

    //数据源相关缓存
    private final Map<String, DatabaseDriverInfo> databaseDriverInfoMap = new HashMap<>();
    private final Map<String, DataSourceMeta> dataSourceMetaMap = new LinkedHashMap<>();
    private final Map<String, DriverInfo> driverInfoMap = new HashMap<>();
    private final AtomicBoolean dataSourceLoading = new AtomicBoolean();
    private final AtomicLong dataSourceLoadVersion = new AtomicLong();
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

    public long beginDataSourceLoad() {
        return dataSourceLoadVersion.incrementAndGet();
    }

    public boolean isCurrentDataSourceLoad(long version) {
        return dataSourceLoadVersion.get() == version;
    }

    public boolean isDataSourceLoading() {
        return dataSourceLoading.get();
    }

    public final void setDataSourceLoading(boolean loading) {
        dataSourceLoading.set(loading);
        onDataSourceLoadingChanged(loading);
    }

    protected void onDataSourceLoadingChanged(boolean loading) {
    }

    @Override
    protected void dispose() {
        dataSourceLoadVersion.incrementAndGet();
        dataSourceLoading.set(false);
        super.dispose();
    }

    @SuppressWarnings("unchecked")
    private JComboBox<String> comboBox(String key) {
        return (JComboBox<String>) getComponent(JComboBox.class, key);
    }

    public final JComboBox<String> databaseBox() {
        return comboBox("dbBox");
    }

    public final JComboBox<String> databaseTypeBox() {
        return comboBox("dbTypeBox");
    }

    public final JComboBox<String> driverBox() {
        return comboBox("driverBox");
    }

    public final JTextField hostField() {
        return getComponent(JTextField.class, "hostText");
    }

    public final JTextField portField() {
        return getComponent(JTextField.class, "portText");
    }

    public final JTextField databaseNameField() {
        return getComponent(JTextField.class, "dbNameText");
    }

    public final JTextField oidField() {
        return getComponent(JTextField.class, "oidText");
    }

    public final JTextField userField() {
        return getComponent(JTextField.class, "userText");
    }

    public final JTextField passwordField() {
        return getComponent(JTextField.class, "pwdText");
    }

    public final JCheckBox baseDataSourceCheckBox() {
        return getComponent(JCheckBox.class, "baseChx");
    }

    public final JCheckBox developmentDataSourceCheckBox() {
        return getComponent(JCheckBox.class, "devChx");
    }

}
