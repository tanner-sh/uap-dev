package com.tanner.devconfig.action.button.datasource;

import com.tanner.abs.AbstractButtonAction;
import com.tanner.abs.AbstractDialog;
import com.tanner.devconfig.DevConfigDialog;
import com.tanner.devconfig.util.DataSourceUtil;
import com.tanner.prop.entity.DataSourceMeta;
import com.tanner.base.BusinessException;
import com.intellij.openapi.ui.Messages;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Map;


/**
 * 设为基准库
 */
public class SetBaseDataSourceAction extends AbstractButtonAction {

    public SetBaseDataSourceAction(AbstractDialog dialog) {
        super(dialog);
    }

    @Override
    public void doAction(ActionEvent event) throws BusinessException {
        DevConfigDialog dialog = (DevConfigDialog) getDialog();
        DataSourceUtil.ensureDataSourceLoaded(dialog);
        Map<String, DataSourceMeta> map = dialog.getDataSourceMetaMap();
        DataSourceMeta currMeta = dialog.getCurrMeta();
        if (currMeta == null) {
            throw new BusinessException("请选择数据源");
        }
        if (currMeta.isDesign()) {
            Messages.showWarningDialog("design 数据源不能设为基准库", "提示");
            return;
        }
        for (DataSourceMeta meta : map.values()) {
            meta.setBase(meta == currMeta);
        }
        dialog.getComponent(JCheckBox.class, "baseChx").setSelected(true);
        dialog.getComponent(JCheckBox.class, "devChx").setSelected(false);
        DataSourceUtil.saveDesignDataSourceMeta(dialog);
    }
}
