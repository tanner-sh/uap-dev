package com.tanner.devconfig.action.button.datasource;

import com.tanner.abs.AbstractButtonAction;
import com.tanner.abs.AbstractDialog;
import com.tanner.base.BusinessException;
import com.tanner.devconfig.DevConfigDialog;
import com.tanner.devconfig.util.DataSourceUtil;
import com.tanner.prop.entity.DataSourceMeta;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Map;

/**
 * 设为开发库
 */
public class SetDevDataSourceAction extends AbstractButtonAction {

    public SetDevDataSourceAction(AbstractDialog dialog) {
        super(dialog);
    }

    @Override
    public void doAction(ActionEvent event) throws BusinessException {
        DevConfigDialog dialog = (DevConfigDialog) getDialog();
        String dsname = (String) dialog.getComponent(JComboBox.class, "dbBox").getSelectedItem();
        int index = dialog.getComponent(JComboBox.class, "dbBox").getSelectedIndex();
        if (index < 0 || "design".equals(dsname)) {
            return;
        }
        if (StringUtils.isNotBlank(dsname)) {
            try {
                Map<String, DataSourceMeta> dataSourceMetaMap = dialog.getDataSourceMetaMap();
                DataSourceMeta meta = (DataSourceMeta) dataSourceMetaMap.get(dsname).clone();
                meta.setDataSourceName("design");
                meta.setBase(false);
                JComboBox dbBox = dialog.getComponent(JComboBox.class, "dbBox");
                boolean hasDesign = dataSourceMetaMap.containsKey("design");
                dataSourceMetaMap.put("design", meta);
                if (!hasDesign) {
                    dbBox.insertItemAt("design", 0);
                }
                dbBox.setSelectedItem("design");
                DataSourceUtil.saveDesignDataSourceMeta(dialog);
            } catch (CloneNotSupportedException e) {
                throw new BusinessException("复制 design 数据源失败: " + e.getMessage());
            }
        }
    }
}
