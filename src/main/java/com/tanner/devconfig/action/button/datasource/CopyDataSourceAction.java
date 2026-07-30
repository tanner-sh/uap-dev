package com.tanner.devconfig.action.button.datasource;

import com.tanner.abs.AbstractButtonAction;
import com.tanner.abs.AbstractDialog;
import com.tanner.base.BusinessException;
import com.tanner.devconfig.DataSourceCopyDlg;
import com.tanner.devconfig.DevConfigDialog;
import com.tanner.devconfig.util.DataSourceUtil;

import java.awt.event.ActionEvent;

/**
 * 复制数据源
 */
public class CopyDataSourceAction extends AbstractButtonAction {

    public CopyDataSourceAction(AbstractDialog dialog) {
        super(dialog);
    }

    @Override
    public void doAction(ActionEvent event) throws BusinessException {
        DevConfigDialog dialog = (DevConfigDialog) getDialog();
        DataSourceUtil.ensureDataSourceLoaded(dialog);
        DataSourceCopyDlg dlg = new DataSourceCopyDlg(dialog);
        dlg.show();
    }

}
