package com.tanner.devconfig.action.button.datasource;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.tanner.abs.AbstractButtonAction;
import com.tanner.abs.AbstractDataSourceDialog;
import com.tanner.abs.AbstractDialog;
import com.tanner.base.ClassLoaderUtil;
import com.tanner.base.DbUtil;
import com.tanner.base.ProjectManager;
import com.tanner.base.UapProjectEnvironment;
import com.tanner.dbdriver.entity.DriverInfo;
import com.tanner.devconfig.DevConfigDialog;
import com.tanner.prop.entity.DataSourceMeta;
import com.tanner.prop.entity.ToolUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;

/**
 * 测试数据源连接
 */
public class TestConnectionAction extends AbstractButtonAction {

    public TestConnectionAction(AbstractDialog dialog) {
        super(dialog);
    }

    @Override
    public void doAction(ActionEvent event) {
        Project project = ProjectManager.getInstance().getProject();
        // 创建一个后台任务
        Task.Backgroundable task = new Task.Backgroundable(project, "Testing datasource ...", true) {
            public void run(@NotNull ProgressIndicator indicator) {
                // 设置初始进度
                if (testConnection()) {
                    Notification notification = new Notification("Task Notification Group", "Done", "Test passed !", NotificationType.INFORMATION);
                    Notifications.Bus.notify(notification, project);
                } else {
                    Notification notification = new Notification("Task Notification Group", "Error", "Test connection error !", NotificationType.ERROR);
                    Notifications.Bus.notify(notification, project);
                }
            }
        };
        // 显示进度条并开始执行任务
        ProgressManager.getInstance().run(task);
    }

    private boolean testConnection() {
        AbstractDataSourceDialog dlg = (AbstractDataSourceDialog) getDialog();
        String homePath;
        if (dlg instanceof DevConfigDialog) {
            homePath = dlg.getComponent(JTextField.class, "homeText").getText();
        } else {
            homePath = UapProjectEnvironment.getInstance().getUapHomePath();
        }
        ClassLoader classLoader;
        try {
            classLoader = ClassLoaderUtil.getUapJdbcClassLoader(homePath);
        } catch (Exception e) {
            return false;
        }
        String dsname = (String) getDialog().getComponent(JComboBox.class, "dbBox").getSelectedItem();
        DataSourceMeta dataSourceMeta = null;
        if (StringUtils.isNotBlank(dsname)) {
            dataSourceMeta = ((AbstractDataSourceDialog) getDialog()).getDataSourceMetaMap().get(dsname);
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
        if (StringUtils.containsIgnoreCase(exampleUrl, "oceanbase") && dataSourceMeta != null) {
            jdbcUrl = dataSourceMeta.getDatabaseUrl();
        }
        Connection connection = null;
        try {
            connection = DbUtil.getConnection(classLoader, info.getDriverClass(), jdbcUrl, userName, pwd);
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            DbUtil.closeResource(connection, null, null);
        }
    }

}
