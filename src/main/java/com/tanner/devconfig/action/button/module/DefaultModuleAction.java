package com.tanner.devconfig.action.button.module;

import com.tanner.abs.AbstractButtonAction;
import com.tanner.abs.AbstractDialog;
import com.tanner.base.ModuleFileUtil;
import com.tanner.devconfig.DevConfigDialog;
import com.tanner.devconfig.util.TableModelUtil;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

/**
 * 应用默认按钮
 */
public class DefaultModuleAction extends AbstractButtonAction {

    private final int type;

    public DefaultModuleAction(AbstractDialog dialog, int type) {
        super(dialog);
        this.type = type;
    }

    @Override
    public void doAction(ActionEvent event) {
        if (type == TableModelUtil.MODULE_TYPE_MUST) {
            JTable table = ((DevConfigDialog) getDialog()).requiredModulesTable();
            Set<String> set = ModuleFileUtil.getMustMoudleSet();
            TableModel model = table.getModel();
            for (int i = 0; i < model.getRowCount(); i++) {
                Object obj = model.getValueAt(i, 2);
                if (set.contains(obj)) {
                    model.setValueAt(true, i, 1);
                }
            }
        } else if (type == TableModelUtil.MODULE_TYPE_SEL) {
            DevConfigDialog view = (DevConfigDialog) getDialog();
            JTable table = view.selectedModulesTable();
            JTable mustTable = view.requiredModulesTable();
            TableModel mustModel = mustTable.getModel();
            Set<String> mustNames = new HashSet<>();
            for (int i = 0; i < mustModel.getRowCount(); i++) {
                if (Boolean.TRUE.equals(mustModel.getValueAt(i, 1))) {
                    mustNames.add(String.valueOf(mustModel.getValueAt(i, 2)));
                }
            }
            TableModel selectedModel = table.getModel();
            for (int i = 0; i < selectedModel.getRowCount(); i++) {
                String moduleName = String.valueOf(selectedModel.getValueAt(i, 2));
                selectedModel.setValueAt(mustNames.contains(moduleName), i, 1);
            }
        }
    }
}
