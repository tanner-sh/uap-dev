package com.tanner.devconfig.action.button.datasource;

import com.intellij.openapi.ui.Messages;
import com.tanner.abs.AbstractButtonAction;
import com.tanner.abs.AbstractDialog;
import com.tanner.devconfig.DevConfigDialog;
import com.tanner.devconfig.util.DataSourceUtil;
import com.tanner.base.BusinessException;

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
    public void doAction(ActionEvent event) throws BusinessException {
        DevConfigDialog dialog = (DevConfigDialog) getDialog();
        DataSourceUtil.ensureDataSourceLoaded(dialog);
        if (dialog.getCurrMeta() == null) {
            throw new BusinessException("请选择数据源");
        }
        String dsName = dialog.getCurrMeta().getDataSourceName();
        if ("design".equals(dsName)) {
            Messages.showWarningDialog("design 数据源不能删除", "提示");
            return;
        }
        JComboBox<String> box = dialog.databaseBox();
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
        DataSourceUtil.saveDesignDataSourceMeta(dialog);
        int opt = Messages.showYesNoDialog("Delete success , do you want to exit ？", "提示",
                Messages.getQuestionIcon());
        if (opt == Messages.OK) {
            dialog.close(0);
        }
    }

}
