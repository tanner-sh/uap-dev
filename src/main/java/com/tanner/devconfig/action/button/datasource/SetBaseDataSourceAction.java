package com.tanner.devconfig.action.button.datasource;

import com.tanner.abs.AbstractButtonAction;
import com.tanner.abs.AbstractDialog;
import com.tanner.devconfig.DevConfigDialog;
import com.tanner.devconfig.util.DataSourceUtil;
import com.tanner.prop.entity.DataSourceMeta;
import java.awt.event.ActionEvent;
import java.util.Map;
import javax.swing.JCheckBox;


/**
 * 设为基准库
 */
public class SetBaseDataSourceAction extends AbstractButtonAction {

  public SetBaseDataSourceAction(AbstractDialog dialog) {
    super(dialog);
  }

  @Override
  public void doAction(ActionEvent event) {
    DevConfigDialog dialog = (DevConfigDialog) getDialog();
    Map<String, DataSourceMeta> map = dialog.getDataSourceMetaMap();
    DataSourceMeta currMeta = dialog.getCurrMeta();
    for (String key : map.keySet()) {
      DataSourceMeta meta = map.get(key);
      if (key.equals(currMeta.getDataSourceName())) {
        meta.setBase(true);
        dialog.getComponent(JCheckBox.class, "baseChx").setSelected(true);
        dialog.getComponent(JCheckBox.class, "devChx").setSelected(false);
      } else {
        meta.setBase(false);
        dialog.getComponent(JCheckBox.class, "baseChx").setSelected(false);
      }
    }
    DataSourceUtil.saveDesignDataSourceMeta(dialog);
  }
}
