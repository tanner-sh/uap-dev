package com.tanner.datadictionary.action;

import com.intellij.openapi.ui.Messages;
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
import com.tanner.prop.entity.ToolUtils;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.sql.Connection;
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
        String homePath = UapProjectEnvironment.getInstance().getUapHomePath();
        ClassLoader classLoader = ClassLoaderUtil.getUapJdbcClassLoader(homePath);
        Connection connection = DbUtil.getConnection(classLoader, info.getDriverClass(), jdbcUrl, userName, pwd);
        IEngine engine = DbUtil.getEngine(connection);
        List<TableInfo> tableInfoList = engine.getAllTableInfo(connection, userName, tableNamePattern);
        DbUtil.closeResource(connection, null, null);
        for (int i = 0; i < tableInfoList.size(); i++) {
            Vector<Object> rowData = new Vector<Object>();
            rowData.add(i + 1);
            rowData.add(true);
            rowData.add(tableInfoList.get(i).getTableName());
            rowData.add(tableInfoList.get(i).getComment());
            ((DefaultTableModel) dbTable.getModel()).addRow(rowData);
        }
        Messages.showInfoMessage("加载完毕", "提示");
    }

}
