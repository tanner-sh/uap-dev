package com.tanner.devconfig.action.button.datasource;

import com.tanner.abs.AbstractButtonAction;
import com.tanner.abs.AbstractDialog;
import com.tanner.devconfig.DataSourceCopyDlg;
import com.tanner.devconfig.DevConfigDialog;

import java.awt.event.ActionEvent;

/**
 * 复制数据源
 */
public class CopyDataSourceAction extends AbstractButtonAction {

    public CopyDataSourceAction(AbstractDialog dialog) {
        super(dialog);
    }

    @Override
    public void doAction(ActionEvent event) {
        DevConfigDialog dialog = (DevConfigDialog) getDialog();
        DataSourceCopyDlg dlg = new DataSourceCopyDlg();
        dlg.setParentDlg(dialog);
        dlg.show();
    }

}
