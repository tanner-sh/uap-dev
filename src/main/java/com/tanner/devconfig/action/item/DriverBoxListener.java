package com.tanner.devconfig.action.item;

import com.tanner.abs.AbstractDialog;
import com.tanner.abs.AbstractItemListener;
import com.tanner.dbdriver.entity.DriverInfo;
import com.tanner.devconfig.DevConfigDialog;
import com.tanner.devconfig.util.DataSourceUtil;
import com.tanner.prop.entity.ToolUtils;
import java.awt.event.ItemEvent;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import org.apache.commons.lang.StringUtils;

/**
 * 驱动类型下拉监听
 */
public class DriverBoxListener extends AbstractItemListener {

  public DriverBoxListener(AbstractDialog dialog) {
    super(dialog);
  }

  @Override
  public void afterSelect(ItemEvent e) {
    DevConfigDialog dialog = (DevConfigDialog) getDialog();
    String selected = (String) dialog.getComponent(JComboBox.class, "driverBox").getSelectedItem();
    if (StringUtils.isNotBlank(selected)) {
      DriverInfo info = dialog.getDriverInfoMap().get(selected);
      dialog.getComponent(JTextField.class, "hostText")
          .setEnabled(ToolUtils.isJDBCUrl(info.getDriverUrl()));
      dialog.getComponent(JTextField.class, "portText")
          .setEnabled(ToolUtils.isJDBCUrl(info.getDriverUrl()));
      DataSourceUtil.fillDBConnByInfo(dialog, info.getDriverUrl());
    }
  }
}
