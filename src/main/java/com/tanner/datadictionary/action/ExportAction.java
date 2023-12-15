package com.tanner.datadictionary.action;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.tanner.abs.AbstractButtonAction;
import com.tanner.abs.AbstractDataSourceDialog;
import com.tanner.abs.AbstractDialog;
import com.tanner.base.BusinessException;
import com.tanner.base.ClassLoaderUtil;
import com.tanner.base.DbUtil;
import com.tanner.base.ProjectManager;
import com.tanner.base.UapProjectEnvironment;
import com.tanner.datadictionary.entity.TableInfo;
import com.tanner.datadictionary.tool.DataDictionaryExportTool;
import com.tanner.dbdriver.entity.DriverInfo;
import com.tanner.prop.entity.ToolUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.sql.Connection;
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
            Messages.showInfoMessage("请选择至少一个!", "提示");
            return;
        }
        File desktopPath = new File(System.getProperty("user.home") + File.separator + "Desktop");
        FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        VirtualFile virtualFile = FileChooser.chooseFile(descriptor, ProjectManager.getInstance().getProject(),
                LocalFileSystem.getInstance().findFileByIoFile(desktopPath));
        if (virtualFile == null) {
            return;
        }
        String driverName = (String) dlg.getComponent(JComboBox.class, "driverBox").getSelectedItem();
        DriverInfo info = dlg.getDriverInfoMap().get(driverName);
        String exampleUrl = info.getDriverUrl();
        String host = dlg.getComponent(JTextField.class, "hostText").getText();
        String port = dlg.getComponent(JTextField.class, "portText").getText();
        String userName = dlg.getComponent(JTextField.class, "userText").getText();
        String pwd = dlg.getComponent(JTextField.class, "pwdText").getText();
        String dbName = dlg.getComponent(JTextField.class, "dbNameText").getText();
        String jdbcUrl = ToolUtils.getJDBCUrl(exampleUrl, dbName, host, port);
        String homePath = UapProjectEnvironment.getInstance().getUapHomePath();
        ClassLoader classLoader = ClassLoaderUtil.getUapJdbcClassLoader(homePath);
        Connection connection = DbUtil.getConnection(classLoader, info.getDriverClass(), jdbcUrl, userName, pwd);
        String exportAs = (String) dlg.getComponent(JComboBox.class, "exportAsBox").getSelectedItem();
        boolean needFilterDefField = dlg.getComponent(JCheckBox.class, "needFilterDefField").isSelected();
        JTextField logTextField = dlg.getComponent(JTextField.class, "logTextField");
        try {
            new DataDictionaryExportTool(connection)
                    .export(virtualFile.getPath(), selectedTables, exportAs, needFilterDefField);
            logTextField.setText("导出完毕!");
        } catch (Exception e) {
            String msg = "导出过程异常\n" + e.getMessage();
            logTextField.setText(msg);
            Messages.showWarningDialog(msg, "错误");
            return;
        }
        Messages.showInfoMessage("导出完毕", "提示");
    }

}
