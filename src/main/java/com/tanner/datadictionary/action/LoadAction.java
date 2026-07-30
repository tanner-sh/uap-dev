package com.tanner.datadictionary.action;

import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.ProcessCanceledException;
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
import com.tanner.devconfig.util.DataSourceUtil;
import com.tanner.ui.BulkTableModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.net.URLClassLoader;
import java.util.List;
import java.util.ArrayList;

public class LoadAction extends AbstractButtonAction {

    public LoadAction(AbstractDialog dialog) {
        super(dialog);
    }

    @Override
    public void doAction(ActionEvent event) throws BusinessException {
        DataDictionaryExportDlg dlg = (DataDictionaryExportDlg) getDialog();
        DataSourceUtil.ensureDataSourceLoaded(dlg);
        JButton loadButton = dlg.loadButton();
        if (!loadButton.isEnabled()) {
            return;
        }
        String driverName = (String) dlg.driverBox().getSelectedItem();
        DriverInfo info = dlg.getDriverInfoMap().get(driverName);
        if (info == null) {
            throw new BusinessException("请选择数据库驱动");
        }
        String exampleUrl = info.getDriverUrl();
        String host = dlg.hostField().getText();
        String port = dlg.portField().getText();
        String userName = dlg.userField().getText();
        String pwd = dlg.passwordField().getText();
        String dbName = dlg.databaseNameField().getText();
        String jdbcUrl = ToolUtils.getJDBCUrl(exampleUrl, dbName, host, port);
        //加载表数据
        JTable dbTable = dlg.table();
        JTextField filterTextField = dlg.filterField();
        String filterTest = filterTextField.getText();
        String[] tableNamePattern = StringUtils.split(filterTest, ";");
        if (dbTable.getModel() instanceof BulkTableModel model) {
            model.clearRows();
        } else {
            ((DefaultTableModel) dbTable.getModel()).setRowCount(0);
        }
        String dsname = (String) dlg.databaseBox().getSelectedItem();
        DataSourceMeta dataSourceMeta = null;
        if (StringUtils.isNotBlank(dsname)) {
            dataSourceMeta = dlg.getDataSourceMetaMap().get(dsname);
        }
        String homePath = UapProjectEnvironment.getInstance(
                getDialog().getProjectContext()).getUapHomePath();
        if (Strings.CI.contains(exampleUrl, "oceanbase") && dataSourceMeta != null) {
            jdbcUrl = dataSourceMeta.getDatabaseUrl();
        }
        String finalJdbcUrl = jdbcUrl;
        Project project = getDialog().getProjectContext();
        JLabel statusLabel = dlg.statusLabel();
        loadButton.setEnabled(false);
        filterTextField.setEnabled(false);
        if (statusLabel != null) {
            statusLabel.setText("正在加载数据表…");
        }
        if (dbTable instanceof com.intellij.ui.table.JBTable jbTable) {
            jbTable.setPaintBusy(true);
        }
        Task.Backgroundable task = new Task.Backgroundable(project, "正在加载数据表…",
                true) {
            private List<TableInfo> result;
            private Exception failure;

            private boolean isUnavailable() {
                return dlg.isDialogDisposed() || project != null && project.isDisposed();
            }

            private void restoreUi() {
                if (isUnavailable()) {
                    return;
                }
                loadButton.setEnabled(true);
                filterTextField.setEnabled(true);
                if (dbTable instanceof com.intellij.ui.table.JBTable jbTable) {
                    jbTable.setPaintBusy(false);
                }
            }

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try (URLClassLoader classLoader =
                             ClassLoaderUtil.getUapJdbcClassLoader(homePath);
                     Connection connection = DbUtil.getConnection(classLoader,
                             info.getDriverClass(), finalJdbcUrl, userName, pwd)) {
                    indicator.setIndeterminate(true);
                    IEngine engine = DbUtil.getEngine(connection);
                    result = engine.getAllTableInfo(
                            connection, userName, tableNamePattern, indicator);
                } catch (ProcessCanceledException exception) {
                    throw exception;
                } catch (Exception exception) {
                    failure = exception;
                }
            }

            @Override
            public void onSuccess() {
                if (isUnavailable()) {
                    return;
                }
                restoreUi();
                if (failure != null) {
                    if (statusLabel != null) {
                        statusLabel.setText("加载失败");
                    }
                    Messages.showErrorDialog(failure.getMessage(), "错误");
                    return;
                }
                List<Object[]> rows = new ArrayList<>(result.size());
                for (int i = 0; i < result.size(); i++) {
                    rows.add(new Object[]{i + 1, true, result.get(i).getTableName(),
                            result.get(i).getComment()});
                }
                if (dbTable.getModel() instanceof BulkTableModel model) {
                    model.replaceRows(rows);
                } else {
                    for (Object[] row : rows) {
                        ((DefaultTableModel) dbTable.getModel()).addRow(row);
                    }
                }
                if (statusLabel != null) {
                    statusLabel.setText("已加载 " + result.size() + " 张表");
                }
            }

            @Override
            public void onCancel() {
                if (isUnavailable()) {
                    return;
                }
                restoreUi();
                if (statusLabel != null) {
                    statusLabel.setText("已取消加载");
                }
            }

            @Override
            public void onThrowable(@NotNull Throwable error) {
                if (isUnavailable()) {
                    return;
                }
                restoreUi();
                if (statusLabel != null) {
                    statusLabel.setText("加载失败");
                }
                String message = StringUtils.defaultIfBlank(
                        error.getMessage(), error.getClass().getName());
                Messages.showErrorDialog("加载数据表失败：\n" + message, "错误");
            }
        };
        ProgressManager.getInstance().run(task);
    }

}
