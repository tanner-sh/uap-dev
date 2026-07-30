package com.tanner.devconfig.util;

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

import javax.swing.*;
import java.io.File;
import java.text.MessageFormat;
import java.util.Map;

/**
 * 数据源初始化工具类
 */
public class DataSourceUtil {

    public static void initDataSource(AbstractDataSourceDialog dialog) {
        String homePath = null;
        if (dialog instanceof DevConfigDialog) {
            homePath = dialog.getComponent(JTextField.class, "homeText").getText();
        } else {
            homePath = UapProjectEnvironment.getInstance(dialog.getProjectContext()).getUapHomePath();
        }
        if (StringUtils.isBlank(homePath)) {
            return;
        }
        DatabaseDriverInfo[] driverinfos = null;
        PropXml propXml = new PropXml();
        try {
            dialog.getDatabaseDriverInfoMap().clear();
            dialog.getDriverInfoMap().clear();
            dialog.getDataSourceMetaMap().clear();
            dialog.setCurrMeta(null);
            dialog.getComponent(JComboBox.class, "dbTypeBox")
                    .setModel(new DefaultComboBoxModel());
            dialog.getComponent(JComboBox.class, "driverBox")
                    .setModel(new DefaultComboBoxModel());
            dialog.getComponent(JComboBox.class, "dbBox")
                    .setModel(new DefaultComboBoxModel());
            //数据库类型
            driverinfos = propXml.getDriverSet(homePath).getDatabase();
            fillCombo(dialog.getComponent(JComboBox.class, "dbTypeBox"), driverinfos, dialog);

            //数据源列表
            String filename = homePath + "/ierp/bin/prop.xml";
            File file = new File(filename);
            if (file.exists()) {
                DataSourceMeta[] sourceMetas = propXml.getDSMetaWithDesign(filename, homePath);
                fillCombo(dialog.getComponent(JComboBox.class, "dbBox"), sourceMetas, dialog);
            }
            //做一次值切换，触发监听显示数据源详情
            JComboBox dbBox = dialog.getComponent(JComboBox.class, "dbBox");
            dbBox.setSelectedIndex(-1);
            if (dbBox.getItemCount() > 0) {
                dbBox.setSelectedIndex(0);
            }
        } catch (Exception exception) {
            Messages.showErrorDialog("加载数据源配置失败:\n" + exception.getMessage(), "错误");
        }
    }

    public static void fillCombo(JComboBox combo, Object[] objects, AbstractDataSourceDialog dlg) {
        if (combo == dlg.getComponent(JComboBox.class, "dbBox")) {
            dlg.getDataSourceMetaMap().clear();
        } else if (combo == dlg.getComponent(JComboBox.class, "dbTypeBox")) {
            dlg.getDatabaseDriverInfoMap().clear();
        } else if (combo == dlg.getComponent(JComboBox.class, "driverBox")) {
            dlg.getDriverInfoMap().clear();
        }
        if (objects == null) {
            combo.setModel(new DefaultComboBoxModel());
            return;
        }
        String[] items = new String[objects.length];
        for (int i = 0; i < objects.length; i++) {
            Object obj = objects[i];
            items[i] = obj.toString();
            if (combo == dlg.getComponent(JComboBox.class, "dbBox")) {
                dlg.getDataSourceMetaMap().put(items[i], (DataSourceMeta) obj);
            } else if (combo == dlg.getComponent(JComboBox.class, "dbTypeBox")) {
                dlg.getDatabaseDriverInfoMap().put(items[i], (DatabaseDriverInfo) obj);
            } else if (combo == dlg.getComponent(JComboBox.class, "driverBox")) {
                dlg.getDriverInfoMap().put(items[i], (DriverInfo) obj);
            }
        }
        combo.setModel(new DefaultComboBoxModel(items));
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
        String driverName = (String) dlg.getComponent(JComboBox.class, "driverBox").getSelectedItem();
        DriverInfo info = dlg.getDriverInfoMap().get(driverName);
        if (info == null) {
            throw new BusinessException("请选择数据库驱动");
        }
        String exampleUrl = info.getDriverUrl();
        String host = dlg.getComponent(JTextField.class, "hostText").getText();
        String port = dlg.getComponent(JTextField.class, "portText").getText();
        String oid = dlg.getComponent(JTextField.class, "oidText").getText();
        String userName = dlg.getComponent(JTextField.class, "userText").getText();
        String pwd = dlg.getComponent(JTextField.class, "pwdText").getText();
        String dbName = dlg.getComponent(JTextField.class, "dbNameText").getText();
        if (ToolUtils.isJDBCUrl(exampleUrl)) {
            dlg.getCurrMeta().setDatabaseUrl(ToolUtils.getJDBCUrl(exampleUrl, dbName, host, port));
        } else {
            dlg.getCurrMeta().setDatabaseUrl(ToolUtils.getODBCUrl(exampleUrl, dbName));
        }
        dlg.getCurrMeta().setUser(userName);
        dlg.getCurrMeta().setPassword(pwd);
        dlg.getCurrMeta().setDriverClassName(info.getDriverClass());
        dlg.getCurrMeta()
                .setDatabaseType((String) dlg.getComponent(JComboBox.class, "dbTypeBox").getSelectedItem());
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
                dlg.getComponent(JComboBox.class, "dbTypeBox").setSelectedItem(dt);
                DatabaseDriverInfo data = dlg.getDatabaseDriverInfoMap().get(dt);
                if (data == null) {
                    Messages.showMessageDialog(
                            MessageFormat.format("Can't find the specified type of datasource{0}", dt), "提示",
                            Messages.getInformationIcon());
                } else {
                    DriverInfo[] infos = data.getDatabase();
                    dlg.getComponent(JComboBox.class, "driverBox")
                            .setSelectedItem(findDriverType(meta.getDriverClassName(), infos));
                }
            }
            fillDBConnUrl(dlg, meta.getDatabaseUrl());
            dlg.getComponent(JTextField.class, "oidText")
                    .setText((meta.getOidMark() != null) ? meta.getOidMark() : "XX");
            dlg.getComponent(JTextField.class, "userText")
                    .setText((meta.getUser() != null) ? meta.getUser() : "sa");
            dlg.getComponent(JTextField.class, "pwdText")
                    .setText((meta.getPassword() != null) ? meta.getPassword() : "sa");
            dlg.getComponent(JCheckBox.class, "baseChx").setSelected(meta.isBase());
            dlg.getComponent(JCheckBox.class, "devChx").setSelected(meta.isDesign());
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
            dlg.getComponent(JTextField.class, "hostText").setText(jdbc[0]);
            dlg.getComponent(JTextField.class, "portText").setText(jdbc[1]);
            dlg.getComponent(JTextField.class, "dbNameText").setText(jdbc[2]);
        } else {
            dlg.getComponent(JTextField.class, "hostText").setText("");
            dlg.getComponent(JTextField.class, "portText").setText("");
            dlg.getComponent(JTextField.class, "dbNameText").setText("");
        }

    }

    public static void fillDBConnByInfo(AbstractDataSourceDialog dialog, String driverUrl) {
        if (ToolUtils.isJDBCUrl(driverUrl)) {
            String[] jdbc = ToolUtils.getJDBCInfo(driverUrl);
            dialog.getComponent(JTextField.class, "portText").setText(jdbc[1]);
        } else {
            dialog.getComponent(JTextField.class, "portText").setText("");
        }
    }


    /**
     * 数据源保存
     *
     * @param dlg dlg
     */
    public static void saveDesignDataSourceMeta(DevConfigDialog dlg) throws BusinessException {
        try {
            UapProjectEnvironment service = UapProjectEnvironment.getInstance(
                    dlg.getProjectContext());
            if (dlg.getCurrMeta() != null) {
                syncCurrDataSourceValue(dlg);
            }
            String nchome = service.getUapHomePath();
            if (StringUtils.isBlank(nchome)) {
                nchome = dlg.getComponent(JTextField.class, "homeText").getText();
            }
            if (StringUtils.isBlank(nchome)) {
                throw new BusinessException("请先设置 NC Home");
            }
            String filename = nchome + "/ierp/bin/prop.xml";
            File file = new File(filename);
            if (!file.exists()) {
                throw new BusinessException("找不到数据源配置: " + filename);
            }
            JComboBox dbBox = dlg.getComponent(JComboBox.class, "dbBox");
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

    static DataSourceMeta[] collectDataSourcesForSave(JComboBox dbBox,
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
