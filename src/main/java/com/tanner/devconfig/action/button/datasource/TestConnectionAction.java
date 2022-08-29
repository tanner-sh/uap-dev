package com.tanner.devconfig.action.button.datasource;

import com.intellij.openapi.ui.Messages;
import com.tanner.abs.AbstractButtonAction;
import com.tanner.abs.AbstractDataSourceDialog;
import com.tanner.abs.AbstractDialog;
import com.tanner.base.ClassLoaderUtil;
import com.tanner.base.DbUtil;
import com.tanner.base.UapProjectEnvironment;
import com.tanner.dbdriver.entity.DriverInfo;
import com.tanner.devconfig.DevConfigDialog;
import com.tanner.prop.entity.ToolUtils;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import javax.swing.JComboBox;
import javax.swing.JTextField;

/**
 * 测试数据源连接
 */
public class TestConnectionAction extends AbstractButtonAction {

  public TestConnectionAction(AbstractDialog dialog) {
    super(dialog);
  }

  @Override
  public void doAction(ActionEvent event) {
    AbstractDataSourceDialog dlg = (AbstractDataSourceDialog) getDialog();
    String homePath = null;
    if (dlg instanceof DevConfigDialog) {
      homePath = dlg.getComponent(JTextField.class, "homeText").getText();
    } else {
      homePath = UapProjectEnvironment.getInstance().getUapHomePath();
    }
    ClassLoader classLoader = null;
    try {
      classLoader = ClassLoaderUtil.getUapJdbcClassLoader(homePath);
    } catch (Exception e) {
      Messages.showMessageDialog("驱动加载失败", "错误", Messages.getErrorIcon());
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
    Connection connection = null;
    try {
      connection = DbUtil.getConnection(classLoader, info.getDriverClass(),
          ToolUtils.getJDBCUrl(exampleUrl, dbName, host, port), userName, pwd);
    } catch (Exception e) {
      Messages.showMessageDialog("获取数据库连接失败\n" + e.getMessage(), "错误",
          Messages.getErrorIcon());
      return;
    } finally {
      DbUtil.closeResource(connection, null, null);
    }
    Messages.showMessageDialog("测试通过", "提示", Messages.getInformationIcon());
  }

}
