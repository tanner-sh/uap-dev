package com.tanner.devconfig.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.ui.Messages;
import com.tanner.abs.AbstractDataSourceDialog;
import com.tanner.base.BusinessException;
import com.tanner.base.UapProjectEnvironment;
import com.tanner.dbdriver.entity.DatabaseDriverInfo;
import com.tanner.dbdriver.entity.DriverInfo;
import com.tanner.devconfig.DevConfigDialog;
import com.tanner.prop.entity.DataSourceMeta;
import com.tanner.prop.entity.ToolUtils;
import com.tanner.prop.xml.PropXml;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import javax.swing.*;
import java.io.File;
import java.text.MessageFormat;
import java.util.Map;

/**
 * 数据源初始化工具类
 */
public class DataSourceUtil {

    public static void initDataSource(AbstractDataSourceDialog dialog) {
        String homePath = getHomePath(dialog);
        if (StringUtils.isBlank(homePath)) {
            return;
        }
        try {
            applyDataSourceSnapshot(dialog, loadDataSourceSnapshot(homePath));
        } catch (Exception exception) {
            Messages.showErrorDialog("加载数据源配置失败:\n" + exception.getMessage(), "错误");
        }
    }

    public static void initDataSourceAsync(AbstractDataSourceDialog dialog) {
        String homePath = getHomePath(dialog);
        if (StringUtils.isBlank(homePath)) {
            return;
        }
        long loadVersion = dialog.beginDataSourceLoad();
        setLoading(dialog, true);
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            DataSourceSnapshot snapshot = null;
            Exception failure = null;
            try {
                snapshot = loadDataSourceSnapshot(homePath);
            } catch (Exception exception) {
                failure = exception;
            }
            DataSourceSnapshot finalSnapshot = snapshot;
            Exception finalFailure = failure;
            ApplicationManager.getApplication().invokeLater(() -> {
                if (dialog.isDialogDisposed()
                        || (dialog.getProjectContext() != null
                        && dialog.getProjectContext().isDisposed())) {
                    return;
                }
                if (!dialog.isCurrentDataSourceLoad(loadVersion)) {
                    return;
                }
                if (!Strings.CS.equals(homePath, getHomePath(dialog))) {
                    setLoading(dialog, false);
                    return;
                }
                try {
                    if (finalFailure != null) {
                        Messages.showErrorDialog(dialog.getProjectContext(),
                                "加载数据源配置失败:\n" + finalFailure.getMessage(), "错误");
                    } else {
                        applyDataSourceSnapshot(dialog, finalSnapshot);
                    }
                } catch (RuntimeException exception) {
                    Messages.showErrorDialog(dialog.getProjectContext(),
                            "更新数据源界面失败:\n" + exception.getMessage(), "错误");
                } finally {
                    setLoading(dialog, false);
                }
            }, ModalityState.any());
        });
    }

    private static String getHomePath(AbstractDataSourceDialog dialog) {
        String homePath;
        if (dialog instanceof DevConfigDialog devConfigDialog) {
            homePath = devConfigDialog.homeField().getText();
        } else {
            homePath = UapProjectEnvironment.getInstance(dialog.getProjectContext()).getUapHomePath();
        }
        return homePath;
    }

    static DataSourceSnapshot loadDataSourceSnapshot(String homePath) throws Exception {
        PropXml propXml = new PropXml();
        DatabaseDriverInfo[] driverInfos = propXml.getDriverSet(homePath).getDatabase();
        DataSourceMeta[] sourceMetas = new DataSourceMeta[0];
        String filename = homePath + "/ierp/bin/prop.xml";
        File file = new File(filename);
        if (file.exists()) {
            sourceMetas = propXml.getDSMetaWithDesign(filename, homePath);
        }
        return new DataSourceSnapshot(driverInfos, sourceMetas);
    }

    private static void applyDataSourceSnapshot(AbstractDataSourceDialog dialog,
                                                DataSourceSnapshot snapshot) {
        dialog.getDatabaseDriverInfoMap().clear();
        dialog.getDriverInfoMap().clear();
        dialog.getDataSourceMetaMap().clear();
        dialog.setCurrMeta(null);
        dialog.databaseTypeBox().setModel(new DefaultComboBoxModel<>());
        dialog.driverBox().setModel(new DefaultComboBoxModel<>());
        dialog.databaseBox().setModel(new DefaultComboBoxModel<>());
        fillCombo(dialog.databaseTypeBox(),
                snapshot.driverInfos(), dialog);
        fillCombo(dialog.databaseBox(),
                snapshot.sourceMetas(), dialog);
        JComboBox<String> dbBox = dialog.databaseBox();
        dbBox.setSelectedIndex(-1);
        if (dbBox.getItemCount() > 0) {
            dbBox.setSelectedIndex(0);
        }
    }

    private static void setLoading(AbstractDataSourceDialog dialog, boolean loading) {
        dialog.setDataSourceLoading(loading);
        for (String key : new String[]{
                "dbBox", "dbTypeBox", "driverBox",
                "hostText", "portText", "dbNameText", "oidText", "userText", "pwdText",
                "testBtn", "copyBtn", "delBtn", "setDevBtn", "setBaseBtn",
                "loadBtn", "exportBtn"
        }) {
            JComponent component = dialog.getComponent(JComponent.class, key);
            if (component != null) {
                component.setEnabled(!loading);
            }
        }
        JComboBox<String> dbBox = dialog.databaseBox();
        if (loading && dbBox != null) {
            dialog.setCurrMeta(null);
            dialog.getDatabaseDriverInfoMap().clear();
            dialog.getDriverInfoMap().clear();
            dialog.getDataSourceMetaMap().clear();
            dialog.databaseTypeBox().setModel(new DefaultComboBoxModel<>());
            dialog.driverBox().setModel(new DefaultComboBoxModel<>());
            dbBox.setModel(new DefaultComboBoxModel<>(new String[]{"正在加载…"}));
            for (JTextField field : new JTextField[]{
                    dialog.hostField(), dialog.portField(), dialog.databaseNameField(),
                    dialog.oidField(), dialog.userField(), dialog.passwordField()
            }) {
                if (field != null) {
                    field.setText("");
                }
            }
            JCheckBox baseCheckBox = dialog.baseDataSourceCheckBox();
            JCheckBox devCheckBox = dialog.developmentDataSourceCheckBox();
            if (baseCheckBox != null) {
                baseCheckBox.setSelected(false);
            }
            if (devCheckBox != null) {
                devCheckBox.setSelected(false);
            }
        } else if (dbBox != null && dbBox.getItemCount() == 1
                && "正在加载…".equals(dbBox.getItemAt(0))) {
            dbBox.setModel(new DefaultComboBoxModel<>());
        }
    }

    record DataSourceSnapshot(DatabaseDriverInfo[] driverInfos,
                              DataSourceMeta[] sourceMetas) {
    }

    public static void fillCombo(JComboBox<String> combo, Object[] objects,
                                 AbstractDataSourceDialog dlg) {
        if (combo == dlg.databaseBox()) {
            dlg.getDataSourceMetaMap().clear();
        } else if (combo == dlg.databaseTypeBox()) {
            dlg.getDatabaseDriverInfoMap().clear();
        } else if (combo == dlg.driverBox()) {
            dlg.getDriverInfoMap().clear();
        }
        if (objects == null) {
            combo.setModel(new DefaultComboBoxModel<>());
            return;
        }
        String[] items = new String[objects.length];
        for (int i = 0; i < objects.length; i++) {
            Object obj = objects[i];
            items[i] = obj.toString();
            if (combo == dlg.databaseBox()) {
                dlg.getDataSourceMetaMap().put(items[i], (DataSourceMeta) obj);
            } else if (combo == dlg.databaseTypeBox()) {
                dlg.getDatabaseDriverInfoMap().put(items[i], (DatabaseDriverInfo) obj);
            } else if (combo == dlg.driverBox()) {
                dlg.getDriverInfoMap().put(items[i], (DriverInfo) obj);
            }
        }
        combo.setModel(new DefaultComboBoxModel<>(items));
    }

    /**
     * 更新当前选中数据源
     *
     * @param dlg
     */
    public static void syncCurrDataSourceValue(DevConfigDialog dlg) throws BusinessException {
        if (dlg.getCurrMeta() == null) {
            throw new BusinessException("请选择数据源");
        }
        String driverName = (String) dlg.driverBox().getSelectedItem();
        DriverInfo info = dlg.getDriverInfoMap().get(driverName);
        if (info == null) {
            throw new BusinessException("请选择数据库驱动");
        }
        String exampleUrl = info.getDriverUrl();
        String host = dlg.hostField().getText();
        String port = dlg.portField().getText();
        String oid = dlg.oidField().getText();
        String userName = dlg.userField().getText();
        String pwd = dlg.passwordField().getText();
        String dbName = dlg.databaseNameField().getText();
        if (ToolUtils.isJDBCUrl(exampleUrl)) {
            dlg.getCurrMeta().setDatabaseUrl(ToolUtils.getJDBCUrl(exampleUrl, dbName, host, port));
        } else {
            dlg.getCurrMeta().setDatabaseUrl(ToolUtils.getODBCUrl(exampleUrl, dbName));
        }
        dlg.getCurrMeta().setUser(userName);
        dlg.getCurrMeta().setPassword(pwd);
        dlg.getCurrMeta().setDriverClassName(info.getDriverClass());
        dlg.getCurrMeta()
                .setDatabaseType((String) dlg.databaseTypeBox().getSelectedItem());
        dlg.getCurrMeta().setOidMark(oid);
        dlg.getDataSourceMetaMap().put(dlg.getCurrMeta().getDataSourceName(), dlg.getCurrMeta());
    }

    private static String findDriverType(String driverClass, DriverInfo[] infos) {
        for (DriverInfo info : infos) {
            if (info.getDriverClass().equals(driverClass)) {
                return info.getDriverType();
            }
        }
        return "";
    }

    /**
     * 填充数据源信息到界面
     *
     * @param dlg dlg
     */
    public static void fillDataSourceMeta(AbstractDataSourceDialog dlg) {
        DataSourceMeta meta = dlg.getCurrMeta();
        if (meta != null) {
            String dbtye = meta.getDatabaseType();
            if (dbtye != null) {
                String dt = dbtye.split("-")[0];
                dlg.databaseTypeBox().setSelectedItem(dt);
                DatabaseDriverInfo data = dlg.getDatabaseDriverInfoMap().get(dt);
                if (data == null) {
                    Messages.showMessageDialog(
                            MessageFormat.format("找不到指定的数据源类型：{0}", dt), "提示",
                            Messages.getInformationIcon());
                } else {
                    DriverInfo[] infos = data.getDatabase();
                    dlg.driverBox().setSelectedItem(
                            findDriverType(meta.getDriverClassName(), infos));
                }
            }
            fillDBConnUrl(dlg, meta.getDatabaseUrl());
            dlg.oidField().setText((meta.getOidMark() != null) ? meta.getOidMark() : "XX");
            dlg.userField().setText((meta.getUser() != null) ? meta.getUser() : "sa");
            dlg.passwordField().setText(
                    (meta.getPassword() != null) ? meta.getPassword() : "sa");
            JCheckBox baseCheckBox = dlg.baseDataSourceCheckBox();
            JCheckBox devCheckBox = dlg.developmentDataSourceCheckBox();
            if (baseCheckBox != null) {
                baseCheckBox.setSelected(meta.isBase());
            }
            if (devCheckBox != null) {
                devCheckBox.setSelected(meta.isDesign());
            }
        }

    }

    /**
     * 填充数据库地址信息
     *
     * @param dlg dlg
     * @param url url
     */
    private static void fillDBConnUrl(AbstractDataSourceDialog dlg, String url) {
        if (ToolUtils.isJDBCUrl(url)) {
            String[] jdbc = ToolUtils.getJDBCInfo(url);
            dlg.hostField().setText(jdbc[0]);
            dlg.portField().setText(jdbc[1]);
            dlg.databaseNameField().setText(jdbc[2]);
        } else {
            dlg.hostField().setText("");
            dlg.portField().setText("");
            dlg.databaseNameField().setText("");
        }

    }

    public static void fillDBConnByInfo(AbstractDataSourceDialog dialog, String driverUrl) {
        if (ToolUtils.isJDBCUrl(driverUrl)) {
            String[] jdbc = ToolUtils.getJDBCInfo(driverUrl);
            dialog.portField().setText(jdbc[1]);
        } else {
            dialog.portField().setText("");
        }
    }


    /**
     * 数据源保存
     *
     * @param dlg dlg
     */
    public static void saveDesignDataSourceMeta(DevConfigDialog dlg) throws BusinessException {
        ensureDataSourceLoaded(dlg);
        try {
            UapProjectEnvironment service = UapProjectEnvironment.getInstance(
                    dlg.getProjectContext());
            if (dlg.getCurrMeta() != null) {
                syncCurrDataSourceValue(dlg);
            }
            String nchome = dlg.homeField().getText();
            if (StringUtils.isBlank(nchome)) {
                nchome = service.getUapHomePath();
            }
            if (StringUtils.isBlank(nchome)) {
                throw new BusinessException("请先设置 NC Home");
            }
            String filename = nchome + "/ierp/bin/prop.xml";
            File file = new File(filename);
            if (!file.exists()) {
                throw new BusinessException("找不到数据源配置: " + filename);
            }
            JComboBox<String> dbBox = dlg.databaseBox();
            DataSourceMeta[] metas = collectDataSourcesForSave(dbBox,
                    dlg.getDataSourceMetaMap());
            new PropXml().saveMeta(filename, metas, nchome);
        } catch (Exception e) {
            if (e instanceof BusinessException businessException) {
                throw businessException;
            }
            throw new BusinessException("保存数据源配置失败:\n" + e.getMessage());
        }
    }

    public static void ensureDataSourceLoaded(AbstractDataSourceDialog dialog)
            throws BusinessException {
        if (dialog.isDataSourceLoading()) {
            throw new BusinessException("数据源配置正在加载，请稍候");
        }
    }

    static DataSourceMeta[] collectDataSourcesForSave(JComboBox<String> dbBox,
                                                       Map<String, DataSourceMeta> sourceMap)
            throws BusinessException {
        DataSourceMeta[] metas = new DataSourceMeta[dbBox.getItemCount()];
        for (int i = 0; i < metas.length; i++) {
            Object item = dbBox.getItemAt(i);
            metas[i] = sourceMap.get(String.valueOf(item));
            if (metas[i] == null) {
                throw new BusinessException("数据源列表与配置不一致: " + item);
            }
        }
        return metas;
    }

}
