package com.tanner.devconfig.action.button.datasource;

import com.intellij.openapi.ui.Messages;
import com.tanner.abs.AbstractButtonAction;
import com.tanner.abs.AbstractDialog;
import com.tanner.devconfig.DevConfigDialog;
import com.tanner.dbdriver.entity.DriverInfo;
import com.tanner.prop.entity.ToolUtils;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Properties;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;

/**
 * 测试数据源连接
 */
public class TestConnectionAction extends AbstractButtonAction {

  public TestConnectionAction(AbstractDialog dialog) {
    super(dialog);
  }

  @Override
  public void doAction(ActionEvent event) {
    DevConfigDialog dlg = (DevConfigDialog) getDialog();
    String driverName = (String) dlg.getComponent(JComboBox.class, "driverBox").getSelectedItem();
    DriverInfo info = dlg.getDriverInfoMap().get(driverName);
    String exampleUrl = info.getDriverUrl();
    String host = dlg.getComponent(JTextField.class, "hostText").getText();
    String port = dlg.getComponent(JTextField.class, "portText").getText();
    String userName = dlg.getComponent(JTextField.class, "userText").getText();
    String pwd = dlg.getComponent(JTextField.class, "pwdText").getText();
    String dbName = dlg.getComponent(JTextField.class, "dbNameText").getText();
    ClassLoader loader = getJDBCClassloader();
    if (loader == null) {
      Messages.showMessageDialog("驱动加载失败", "错误", Messages.getErrorIcon());
      return;
    }
    Connection conn = null;
    try {
      Class driverclass = loader.loadClass(info.getDriverClass());
      Driver deiver = (Driver) driverclass.getConstructor().newInstance();
      Properties properties = new Properties();
      properties.put("user", userName);
      properties.put("password", pwd);
      conn = deiver.connect(ToolUtils.getJDBCUrl(exampleUrl, dbName, host, port), properties);
    } catch (Exception e) {
      Messages.showMessageDialog(e.getMessage(), "测试未能通过请检查", Messages.getErrorIcon());
      return;
    } finally {
      if (conn != null) {
        try {
          conn.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
    if (conn != null) {
      Messages.showMessageDialog("测试通过", "提示", Messages.getInformationIcon());
    }
  }

  private ClassLoader getJDBCClassloader() {
    //加载home/drivers下所有jar包
    String homePath = getDialog().getComponent(JTextField.class, "homeText").getText();
    URLClassLoader loader = null;
    File driverLibDir = new File(homePath, "driver");
    if (!driverLibDir.exists()) {
      return null;
    }
    Collection<File> files = FileUtils.listFiles(driverLibDir, null, true);
    if (CollectionUtils.isEmpty(files)) {
      return null;
    }
    URL[] urls = null;
    try {
      urls = FileUtils.toURLs(files.toArray(new File[0]));
    } catch (IOException e) {
      return null;
    }
    return new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
  }

}
