package com.tanner.devconfig.action.button.module;

import com.tanner.abs.AbstractButtonAction;
import com.tanner.abs.AbstractDialog;
import com.tanner.devconfig.util.TableModelUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * 全消按钮
 */
public class CancelAllAction extends AbstractButtonAction {

    private final int type;

    public CancelAllAction(AbstractDialog dialog, int type) {
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
            TableModelUtil.setAllCheckState(table, false);
        }
    }
}
