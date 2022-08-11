package com.tanner.devconfig.action.button;

import com.tanner.abs.AbstractButtonAction;
import com.tanner.abs.AbstractDialog;
import com.tanner.base.BusinessException;
import com.tanner.devconfig.DevConfigDialog;
import com.tanner.devconfig.util.DataSourceUtil;
import com.tanner.devconfig.util.TableModelUtil;
import java.awt.event.ActionEvent;

/**
 * 数据源保存
 */
public class ApplyAction extends AbstractButtonAction {

  public ApplyAction(AbstractDialog dialog) {
    super(dialog);
  }

  @Override
  public void doAction(ActionEvent event) throws BusinessException {
    DevConfigDialog dialog = (DevConfigDialog) getDialog();

    //数据源保存
    if (dialog.getTabIndex() == 0) {
      DataSourceUtil.syncCurrDataSourceValue(dialog);
    }

    //模块选择保存
    if (dialog.getTabIndex() == 1) {
      TableModelUtil.saveModuleConfig(getDialog());
    }
  }
}
