package com.tanner.datadictionary.action;

import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.tanner.abs.AbstractButtonAction;
import com.tanner.abs.AbstractDataSourceDialog;
import com.tanner.abs.AbstractDialog;
import com.tanner.base.BusinessException;
import com.tanner.base.ClassLoaderUtil;
import com.tanner.base.DbUtil;
import com.tanner.base.UapProjectEnvironment;
import com.tanner.datadictionary.engine.IEngine;
import com.tanner.datadictionary.entity.TableInfo;
import com.tanner.dbdriver.entity.DriverInfo;
import com.tanner.prop.entity.DataSourceMeta;
import com.tanner.prop.entity.ToolUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Vector;

public class LoadAction extends AbstractButtonAction {

    public LoadAction(AbstractDialog dialog) {
        super(dialog);
    }

    @Override
    public void doAction(ActionEvent event) throws BusinessException {
        AbstractDataSourceDialog dlg = (AbstractDataSourceDialog) getDialog();
        String driverName = (String) dlg.getComponent(JComboBox.class, "driverBox").getSelectedItem();
        DriverInfo info = dlg.getDriverInfoMap().get(driverName);
        if (info == null) {
            throw new BusinessException("请选择数据库驱动");
        }
        String exampleUrl = info.getDriverUrl();
        String host = dlg.getComponent(JTextField.class, "hostText").getText();
        String port = dlg.getComponent(JTextField.class, "portText").getText();
        String userName = dlg.getComponent(JTextField.class, "userText").getText();
        String pwd = dlg.getComponent(JTextField.class, "pwdText").getText();
        String dbName = dlg.getComponent(JTextField.class, "dbNameText").getText();
        String jdbcUrl = ToolUtils.getJDBCUrl(exampleUrl, dbName, host, port);
        //加载表数据
        JTable dbTable = getDialog().getComponent(JTable.class, "dbTable");
        String filterTest = getDialog().getComponent(JTextField.class, "filterTextField").getText();
        String[] tableNamePattern = StringUtils.split(filterTest, ";");
        for (int rowCount = dbTable.getModel().getRowCount(); rowCount > 0; rowCount--) {
            ((DefaultTableModel) dbTable.getModel()).removeRow(rowCount - 1);
        }
        String dsname = (String) getDialog().getComponent(JComboBox.class, "dbBox").getSelectedItem();
        DataSourceMeta dataSourceMeta = null;
        if (StringUtils.isNotBlank(dsname)) {
            dataSourceMeta = ((AbstractDataSourceDialog) getDialog()).getDataSourceMetaMap().get(dsname);
        }
        String homePath = UapProjectEnvironment.getInstance(
                getDialog().getProjectContext()).getUapHomePath();
        if (StringUtils.containsIgnoreCase(exampleUrl, "oceanbase") && dataSourceMeta != null) {
            jdbcUrl = dataSourceMeta.getDatabaseUrl();
        }
        String finalJdbcUrl = jdbcUrl;
        Project project = getDialog().getProjectContext();
        JButton loadButton = getDialog().getComponent(JButton.class, "loadBtn");
        loadButton.setEnabled(false);
        Task.Backgroundable task = new Task.Backgroundable(project, "Loading database tables...",
                true) {
            private List<TableInfo> result;
            private Exception failure;

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try (URLClassLoader classLoader =
                             ClassLoaderUtil.getUapJdbcClassLoader(homePath);
                     Connection connection = DbUtil.getConnection(classLoader,
                             info.getDriverClass(), finalJdbcUrl, userName, pwd)) {
                    indicator.setIndeterminate(true);
                    IEngine engine = DbUtil.getEngine(connection);
                    result = engine.getAllTableInfo(connection, userName, tableNamePattern);
                } catch (Exception exception) {
                    failure = exception;
                }
            }

            @Override
            public void onSuccess() {
                loadButton.setEnabled(true);
                if (failure != null) {
                    Messages.showErrorDialog(failure.getMessage(), "错误");
                    return;
                }
                for (int i = 0; i < result.size(); i++) {
                    Vector<Object> rowData = new Vector<>();
                    rowData.add(i + 1);
                    rowData.add(true);
                    rowData.add(result.get(i).getTableName());
                    rowData.add(result.get(i).getComment());
                    ((DefaultTableModel) dbTable.getModel()).addRow(rowData);
                }
                Messages.showInfoMessage("Load success!", "提示");
            }

            @Override
            public void onCancel() {
                loadButton.setEnabled(true);
            }
        };
        ProgressManager.getInstance().run(task);
    }

}
