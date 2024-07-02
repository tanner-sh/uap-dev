package com.tanner.script.export.action;

import com.intellij.openapi.ui.Messages;
import com.tanner.abs.AbstractButtonAction;
import com.tanner.abs.AbstractDataSourceDialog;
import com.tanner.abs.AbstractDialog;
import com.tanner.dbdriver.entity.DriverInfo;
import com.tanner.prop.entity.ToolUtils;
import com.tanner.script.export.util.ScriptExportTool;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ExportAction extends AbstractButtonAction {


    public ExportAction(AbstractDialog dialog) {
        super(dialog);
    }

    @Override
    public void doAction(ActionEvent event) {
        AbstractDataSourceDialog dlg = (AbstractDataSourceDialog) getDialog();
        String exportPath = dlg.getComponent(JTextField.class, "exportPathText").getText();
        if (StringUtils.isEmpty(exportPath)) {
            Messages.showWarningDialog("请选择导出路径", "提示");
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
        String heavyNodeCode = getDialog().getComponent(JTextField.class, "heavyNodeCodeText").getText();
        String lightNodeCode = getDialog().getComponent(JTextField.class, "lightNodeCodeText").getText();
        String mdName = getDialog().getComponent(JTextField.class, "mdNameText").getText();
        String mdModule = getDialog().getComponent(JTextField.class, "mdModuleText").getText();
        int exportMode = getDialog().getComponent(JComboBox.class, "exportModeComboBox").getSelectedIndex();
        boolean spiltGo = getDialog().getComponent(JCheckBox.class, "spiltGoCheckBox").isSelected();
        try {
            new ScriptExportTool(info.getDriverClass(), jdbcUrl, userName, pwd, exportMode, spiltGo).export(exportPath,
                    heavyNodeCode, lightNodeCode, mdName, mdModule);
        } catch (Exception e) {
            Messages.showWarningDialog("导出脚本异常\n" + e.getMessage(), "错误");
            return;
        }
        Messages.showInfoMessage("导出完毕", "提示");
    }

}
