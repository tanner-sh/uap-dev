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
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ExportAction extends AbstractButtonAction {


    public ExportAction(AbstractDialog dialog) {
        super(dialog);
    }

    @Override
    public void doAction(ActionEvent event) throws BusinessException {
        AbstractDataSourceDialog dlg = (AbstractDataSourceDialog) getDialog();
        String exportPath = dlg.getComponent(JTextField.class, "exportPathText").getText();
        if (StringUtils.isEmpty(exportPath)) {
            Messages.showWarningDialog("请选择导出路径", "提示");
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
        String heavyNodeCode = getDialog().getComponent(JTextField.class, "heavyNodeCodeText").getText();
        String lightNodeCode = getDialog().getComponent(JTextField.class, "lightNodeCodeText").getText();
        String mdName = getDialog().getComponent(JTextField.class, "mdNameText").getText();
        String mdModule = getDialog().getComponent(JTextField.class, "mdModuleText").getText();
        int exportMode = getDialog().getComponent(JComboBox.class, "exportModeComboBox").getSelectedIndex();
        boolean spiltGo = getDialog().getComponent(JCheckBox.class, "spiltGoCheckBox").isSelected();
        String dsname = (String) getDialog().getComponent(JComboBox.class, "dbBox").getSelectedItem();
        DataSourceMeta dataSourceMeta = null;
        if (StringUtils.isNotBlank(dsname)) {
            dataSourceMeta = ((AbstractDataSourceDialog) getDialog()).getDataSourceMetaMap().get(dsname);
        }
        if (StringUtils.containsIgnoreCase(exampleUrl, "oceanbase") && dataSourceMeta != null) {
            jdbcUrl = dataSourceMeta.getDatabaseUrl();
        }
        String homePath = UapProjectEnvironment.getInstance(
                getDialog().getProjectContext()).getUapHomePath();
        String finalJdbcUrl = jdbcUrl;
        Project project = getDialog().getProjectContext();
        JButton exportButton = getDialog().getComponent(JButton.class, "exportBtn");
        exportButton.setEnabled(false);
        Task.Backgroundable task = new Task.Backgroundable(project, "Exporting SQL scripts...",
                true) {
            private Exception failure;

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
                exportButton.setEnabled(true);
                if (failure != null) {
                    Messages.showWarningDialog("导出脚本异常\n" + failure.getMessage(), "错误");
                } else {
                    Messages.showInfoMessage("导出完毕", "提示");
                }
            }

            @Override
            public void onCancel() {
                exportButton.setEnabled(true);
            }
        };
        ProgressManager.getInstance().run(task);
    }

}
