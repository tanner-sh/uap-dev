package com.tanner.devconfig.action.item;

import com.tanner.abs.AbstractDataSourceDialog;
import com.tanner.abs.AbstractDialog;
import com.tanner.abs.AbstractItemListener;
import com.tanner.devconfig.util.DataSourceUtil;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.event.ItemEvent;

/**
 * 数据源列表下拉监听
 */
public class DBBoxListener extends AbstractItemListener {

    public DBBoxListener(AbstractDialog dialog) {
        super(dialog);
    }

    @Override
    public void afterSelect(ItemEvent e) {
        String dsname = (String) getDialog().getComponent(JComboBox.class, "dbBox").getSelectedItem();
        if (StringUtils.isNotBlank(dsname)) {
            AbstractDataSourceDialog dialog = (AbstractDataSourceDialog) getDialog();
            dialog.setCurrMeta(dialog.getDataSourceMetaMap().get(dsname));
            DataSourceUtil.fillDataSourceMeta(dialog);
        }
    }
}
