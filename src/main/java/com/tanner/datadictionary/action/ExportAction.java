package com.tanner.datadictionary.action;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
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
import com.tanner.datadictionary.entity.TableInfo;
import com.tanner.datadictionary.tool.DataDictionaryExportTool;
import com.tanner.dbdriver.entity.DriverInfo;
import com.tanner.devconfig.util.DataSourceUtil;
import com.tanner.prop.entity.DataSourceMeta;
import com.tanner.prop.entity.ToolUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.sql.Connection;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class ExportAction extends AbstractButtonAction {

    public ExportAction(AbstractDialog dialog) {
        super(dialog);
    }

    @Override
    public void doAction(ActionEvent event) throws BusinessException {
        DataDictionaryExportDlg dlg = (DataDictionaryExportDlg) getDialog();
        DataSourceUtil.ensureDataSourceLoaded(dlg);
        JTable dbTable = dlg.table();
        List<TableInfo> selectedTables = new ArrayList<>();
        for (int row = 0; row < dbTable.getModel().getRowCount(); row++) {
            boolean selected = (boolean) dbTable.getModel().getValueAt(row, 1);
            if (selected) {
                String tableName = (String) dbTable.getModel().getValueAt(row, 2);
                String comment = (String) dbTable.getModel().getValueAt(row, 3);
                selectedTables.add(new TableInfo(tableName, comment));
            }
        }
        if (selectedTables.isEmpty()) {
            Messages.showInfoMessage("请至少选择一张数据表", "提示");
            return;
        }
        File desktopPath = new File(System.getProperty("user.home") + File.separator + "Desktop");
        FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        VirtualFile virtualFile = FileChooser.chooseFile(descriptor,
                getDialog().getProjectContext(),
                LocalFileSystem.getInstance().findFileByIoFile(desktopPath));
        if (virtualFile == null) {
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
        String homePath = UapProjectEnvironment.getInstance(
                getDialog().getProjectContext()).getUapHomePath();
        String dsname = (String) dlg.databaseBox().getSelectedItem();
        DataSourceMeta dataSourceMeta = null;
        if (StringUtils.isNotBlank(dsname)) {
            dataSourceMeta = dlg.getDataSourceMetaMap().get(dsname);
        }
        if (Strings.CI.contains(exampleUrl, "oceanbase") && dataSourceMeta != null) {
            jdbcUrl = dataSourceMeta.getDatabaseUrl();
        }
        String exportAs = (String) dlg.exportFormatBox().getSelectedItem();
        boolean needFilterDefField = dlg.filterDefaultFieldsCheckBox().isSelected();
        JProgressBar progressBar = dlg.progressBar();
        progressBar.setIndeterminate(true);
        JButton exportButton = dlg.exportButton();
        JLabel statusLabel = dlg.statusLabel();
        exportButton.setEnabled(false);
        if (statusLabel != null) {
            statusLabel.setText("正在导出数据字典…");
        }
        Project project = getDialog().getProjectContext();
        String finalJdbcUrl = jdbcUrl;
        Task.Backgroundable task = new Task.Backgroundable(project, "正在导出数据字典…",
                true) {
            private Exception failure;

            private boolean isUnavailable() {
                return dlg.isDialogDisposed() || project != null && project.isDisposed();
            }

            private void restoreUi() {
                if (isUnavailable()) {
                    return;
                }
                progressBar.setIndeterminate(false);
                exportButton.setEnabled(true);
            }

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try (URLClassLoader classLoader =
                             ClassLoaderUtil.getUapJdbcClassLoader(homePath);
                     Connection connection = DbUtil.getConnection(classLoader,
                             info.getDriverClass(), finalJdbcUrl, userName, pwd)) {
                    new DataDictionaryExportTool(connection, indicator)
                            .export(virtualFile.getPath(), selectedTables, exportAs,
                                    needFilterDefField);
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
                        statusLabel.setText("导出失败");
                    }
                    Messages.showWarningDialog("导出过程异常\n" + failure.getMessage(), "错误");
                } else {
                    progressBar.setValue(100);
                    if (statusLabel != null) {
                        statusLabel.setText("导出完成");
                    }
                    Messages.showInfoMessage("数据字典导出完成", "完成");
                }
            }

            @Override
            public void onCancel() {
                if (isUnavailable()) {
                    return;
                }
                restoreUi();
                if (statusLabel != null) {
                    statusLabel.setText("已取消导出");
                }
            }

            @Override
            public void onThrowable(@NotNull Throwable error) {
                if (isUnavailable()) {
                    return;
                }
                restoreUi();
                if (statusLabel != null) {
                    statusLabel.setText("导出失败");
                }
                String message = StringUtils.defaultIfBlank(
                        error.getMessage(), error.getClass().getName());
                Messages.showErrorDialog("导出数据字典失败：\n" + message, "错误");
            }
        };
        ProgressManager.getInstance().run(task);
    }

}
