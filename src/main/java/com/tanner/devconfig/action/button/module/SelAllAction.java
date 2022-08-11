package com.tanner.devconfig.action.button.module;

import com.tanner.abs.AbstractButtonAction;
import com.tanner.abs.AbstractDialog;
import com.tanner.devconfig.util.TableModelUtil;
import java.awt.event.ActionEvent;
import javax.swing.JTable;

/**
 * 全选按钮
 */
public class SelAllAction extends AbstractButtonAction {

  private int type;

  public SelAllAction(AbstractDialog dialog, int type) {
    super(dialog);
    this.type = type;
  }


  @Override
  public void doAction(ActionEvent event) {
    JTable table = null;
    if (type == TableModelUtil.MODULE_TYPE_MUST) {
      table = getDialog().getComponent(JTable.class, "mustTable");
    } else if (type == TableModelUtil.MODULE_TYPE_SEL) {
      table = getDialog().getComponent(JTable.class, "selTable");
    }
    if (table != null) {
      TableModelUtil.setAllCheckState(table, true);
    }
  }
}
