package com.tanner.devconfig.action.item;

import com.tanner.abs.AbstractDialog;
import com.tanner.abs.AbstractItemListener;
import com.tanner.devconfig.DevConfigDialog;
import com.tanner.devconfig.util.DataSourceUtil;
import com.tanner.script.studio.ui.preference.dbdriver.DriverInfo;
import java.awt.event.ItemEvent;
import javax.swing.JComboBox;
import org.apache.commons.lang.StringUtils;

/**
 * 驱动列表下拉监听
 */
public class DBTypeBoxListener extends AbstractItemListener {

  public DBTypeBoxListener(AbstractDialog dialog) {
    super(dialog);
  }

  @Override
  public void afterSelect(ItemEvent e) {
    DevConfigDialog dialog = (DevConfigDialog) getDialog();
    String selected = (String) dialog.getComponent(JComboBox.class, "dbTypeBox").getSelectedItem();
    if (StringUtils.isNotBlank(selected)) {
      DriverInfo[] infos = dialog.getDatabaseDriverInfoMap().get(selected).getDatabase();
      DataSourceUtil.fillCombo(dialog.getComponent(JComboBox.class, "driverBox"), infos, dialog);
    }
  }
}
