package com.tanner.devconfig.action.button.module;

import com.tanner.abs.AbstractButtonAction;
import com.tanner.abs.AbstractDialog;
import com.tanner.base.ModuleFileUtil;
import com.tanner.devconfig.util.TableModelUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;

/**
 * 应用默认按钮
 */
public class DefaultModuleAction extends AbstractButtonAction {

    private int type;

    public DefaultModuleAction(AbstractDialog dialog, int type) {
        super(dialog);
        this.type = type;
    }

    @Override
    public void doAction(ActionEvent event) {
        if (type == TableModelUtil.MODULE_TYPE_MUST) {
            JTable table = getDialog().getComponent(JTable.class, "mustTable");
            Set<String> set = ModuleFileUtil.getMustMoudleSet();
            int count = getDialog().getComponent(JTable.class, "mustTable").getRowCount();
            for (int i = 0; i < count; i++) {
                Object obj = table.getModel().getValueAt(i, 2);
                if (set.contains(obj)) {
                    table.setValueAt(true, i, 1);
                }
            }
        } else if (type == TableModelUtil.MODULE_TYPE_SEL) {
            JTable table = getDialog().getComponent(JTable.class, "selTable");
            JTable mustTable = getDialog().getComponent(JTable.class, "mustTable");
            int count = getDialog().getComponent(JTable.class, "mustTable").getRowCount();
            for (int i = 0; i < count; i++) {
                Object obj = mustTable.getModel().getValueAt(i, 1);
                boolean checked = obj == null ? false : Boolean.valueOf(obj.toString());
                table.setValueAt(checked, i, 1);
            }
        }
    }
}
