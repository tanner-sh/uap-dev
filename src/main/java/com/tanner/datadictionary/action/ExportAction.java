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
import com.tanner.prop.entity.DataSourceMeta;
import com.tanner.prop.entity.ToolUtils;
import org.apache.commons.lang3.StringUtils;
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
        AbstractDataSourceDialog dlg = (AbstractDataSourceDialog) getDialog();
        JTable dbTable = getDialog().getComponent(JTable.class, "dbTable");
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
            Messages.showInfoMessage("You must select one or more!", "提示");
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
        String homePath = UapProjectEnvironment.getInstance(
                getDialog().getProjectContext()).getUapHomePath();
        String dsname = (String) getDialog().getComponent(JComboBox.class, "dbBox").getSelectedItem();
        DataSourceMeta dataSourceMeta = null;
        if (StringUtils.isNotBlank(dsname)) {
            dataSourceMeta = ((AbstractDataSourceDialog) getDialog()).getDataSourceMetaMap().get(dsname);
        }
        if (StringUtils.containsIgnoreCase(exampleUrl, "oceanbase") && dataSourceMeta != null) {
            jdbcUrl = dataSourceMeta.getDatabaseUrl();
        }
        String exportAs = (String) dlg.getComponent(JComboBox.class, "exportAsBox").getSelectedItem();
        boolean needFilterDefField = dlg.getComponent(JCheckBox.class, "needFilterDefField").isSelected();
        JProgressBar progressBar = dlg.getComponent(JProgressBar.class, "progressBar");
        progressBar.setIndeterminate(true);
        JButton exportButton = getDialog().getComponent(JButton.class, "exportBtn");
        exportButton.setEnabled(false);
        Project project = getDialog().getProjectContext();
        String finalJdbcUrl = jdbcUrl;
        Task.Backgroundable task = new Task.Backgroundable(project, "Exporting data dictionary...",
                true) {
            private Exception failure;

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
                progressBar.setIndeterminate(false);
                exportButton.setEnabled(true);
                if (failure != null) {
                    Messages.showWarningDialog("导出过程异常\n" + failure.getMessage(), "错误");
                } else {
                    progressBar.setValue(100);
                    Messages.showInfoMessage("Success", "提示");
                }
            }

            @Override
            public void onCancel() {
                progressBar.setIndeterminate(false);
                exportButton.setEnabled(true);
            }
        };
        ProgressManager.getInstance().run(task);
    }

}
