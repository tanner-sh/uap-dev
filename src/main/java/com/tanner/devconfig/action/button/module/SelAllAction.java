package com.tanner.devconfig.action.button.module;

import com.tanner.abs.AbstractButtonAction;
import com.tanner.abs.AbstractDialog;
import com.tanner.devconfig.DevConfigDialog;
import com.tanner.devconfig.util.TableModelUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * 全选按钮
 */
public class SelAllAction extends AbstractButtonAction {

    private final int type;

    public SelAllAction(AbstractDialog dialog, int type) {
        super(dialog);
        this.type = type;
    }

    @Override
    public void doAction(ActionEvent event) {
        JTable table = null;
        if (type == TableModelUtil.MODULE_TYPE_MUST) {
            table = ((DevConfigDialog) getDialog()).requiredModulesTable();
        } else if (type == TableModelUtil.MODULE_TYPE_SEL) {
            table = ((DevConfigDialog) getDialog()).selectedModulesTable();
        }
        if (table != null) {
            TableModelUtil.setAllCheckState(table, true);
        }
    }
}
