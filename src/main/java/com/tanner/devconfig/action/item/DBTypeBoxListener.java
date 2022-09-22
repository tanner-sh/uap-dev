package com.tanner.devconfig.action.item;

import com.tanner.abs.AbstractDataSourceDialog;
import com.tanner.abs.AbstractDialog;
import com.tanner.abs.AbstractItemListener;
import com.tanner.dbdriver.entity.DriverInfo;
import com.tanner.devconfig.util.DataSourceUtil;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.event.ItemEvent;

/**
 * 驱动列表下拉监听
 */
public class DBTypeBoxListener extends AbstractItemListener {

    public DBTypeBoxListener(AbstractDialog dialog) {
        super(dialog);
    }

    @Override
    public void afterSelect(ItemEvent e) {
        AbstractDataSourceDialog dialog = (AbstractDataSourceDialog) getDialog();
        String selected = (String) dialog.getComponent(JComboBox.class, "dbTypeBox").getSelectedItem();
        if (StringUtils.isNotBlank(selected)) {
            DriverInfo[] infos = dialog.getDatabaseDriverInfoMap().get(selected).getDatabase();
            DataSourceUtil.fillCombo(dialog.getComponent(JComboBox.class, "driverBox"), infos, dialog);
        }
    }
}
