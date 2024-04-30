package com.tanner.devconfig.action.button.datasource;

import com.intellij.openapi.ui.Messages;
import com.tanner.abs.AbstractButtonAction;
import com.tanner.abs.AbstractDialog;
import com.tanner.devconfig.DevConfigDialog;
import com.tanner.devconfig.util.DataSourceUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * 删除数据源
 */
public class DeleteDataSourceAction extends AbstractButtonAction {

    public DeleteDataSourceAction(AbstractDialog dialog) {
        super(dialog);
    }

    @Override
    public void doAction(ActionEvent event) {
        DevConfigDialog dialog = (DevConfigDialog) getDialog();
        String dsName = dialog.getCurrMeta().getDataSourceName();
        JComboBox box = dialog.getComponent(JComboBox.class, "dbBox");
        int index = box.getSelectedIndex();
        int count = box.getItemCount();
        if (count == 1) {
            Messages.showMessageDialog("Can not delete this datasource , because it is only one!", "Tips",
                    Messages.getInformationIcon());
            return;
        }
        if (index == count - 1) {
            index = index - 1;
        }
        box.removeItem(dsName);
        box.setSelectedIndex(index);
        dialog.getDataSourceMetaMap().remove(dsName);
        int opt = Messages.showYesNoDialog("Delete success , do you want to exit ？", "提示",
                Messages.getQuestionIcon());
        if (opt == Messages.OK) {
            DataSourceUtil.saveDesignDataSourceMeta(dialog);
            dialog.close(0);
        }
    }

}
