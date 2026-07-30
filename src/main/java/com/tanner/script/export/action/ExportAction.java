package com.tanner.script.export.action;

import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.tanner.abs.AbstractButtonAction;
import com.tanner.abs.AbstractDataSourceDialog;
import com.tanner.abs.AbstractDialog;
import com.tanner.dbdriver.entity.DriverInfo;
import com.tanner.base.UapProjectEnvironment;
import com.tanner.base.BusinessException;
import com.tanner.prop.entity.DataSourceMeta;
import com.tanner.prop.entity.ToolUtils;
import com.tanner.script.export.util.ScriptExportTool;
import com.tanner.script.export.dlg.ScriptExportDlg;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ExportAction extends AbstractButtonAction {


    public ExportAction(AbstractDialog dialog) {
        super(dialog);
    }

    @Override
    public void doAction(ActionEvent event) throws BusinessException {
        ScriptExportDlg dlg = (ScriptExportDlg) getDialog();
        String exportPath = dlg.exportPathField().getText();
        if (StringUtils.isEmpty(exportPath)) {
            Messages.showWarningDialog("请选择导出路径", "提示");
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
        String heavyNodeCode = dlg.heavyNodeCodeField().getText();
        String lightNodeCode = dlg.lightNodeCodeField().getText();
        String mdName = dlg.metadataNameField().getText();
        String mdModule = dlg.metadataModuleField().getText();
        int exportMode = dlg.exportModeBox().getSelectedIndex();
        boolean spiltGo = dlg.splitGoCheckBox().isSelected();
        String dsname = (String) dlg.databaseBox().getSelectedItem();
        DataSourceMeta dataSourceMeta = null;
        if (StringUtils.isNotBlank(dsname)) {
            dataSourceMeta = ((AbstractDataSourceDialog) getDialog()).getDataSourceMetaMap().get(dsname);
        }
        if (Strings.CI.contains(exampleUrl, "oceanbase") && dataSourceMeta != null) {
            jdbcUrl = dataSourceMeta.getDatabaseUrl();
        }
        String homePath = UapProjectEnvironment.getInstance(
                getDialog().getProjectContext()).getUapHomePath();
        String finalJdbcUrl = jdbcUrl;
        Project project = getDialog().getProjectContext();
        JButton exportButton = dlg.exportButton();
        exportButton.setEnabled(false);
        Task.Backgroundable task = new Task.Backgroundable(project, "Exporting SQL scripts...",
                true) {
            private Exception failure;

            private boolean isUnavailable() {
                return dlg.isDialogDisposed() || project != null && project.isDisposed();
            }

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    indicator.setIndeterminate(true);
                    new ScriptExportTool(homePath, info.getDriverClass(), finalJdbcUrl,
                            userName, pwd, exportMode, spiltGo).export(exportPath,
                            heavyNodeCode, lightNodeCode, mdName, mdModule, indicator);
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
                exportButton.setEnabled(true);
                if (failure != null) {
                    Messages.showWarningDialog("导出脚本异常\n" + failure.getMessage(), "错误");
                } else {
                    Messages.showInfoMessage("导出完毕", "提示");
                }
            }

            @Override
            public void onCancel() {
                if (isUnavailable()) {
                    return;
                }
                exportButton.setEnabled(true);
            }

            @Override
            public void onThrowable(@NotNull Throwable error) {
                if (isUnavailable()) {
                    return;
                }
                exportButton.setEnabled(true);
                String message = StringUtils.defaultIfBlank(
                        error.getMessage(), error.getClass().getName());
                Messages.showErrorDialog("导出脚本异常\n" + message, "错误");
            }
        };
        ProgressManager.getInstance().run(task);
    }

}
