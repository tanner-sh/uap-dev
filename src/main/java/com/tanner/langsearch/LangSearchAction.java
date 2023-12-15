package com.tanner.langsearch;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.tanner.abs.AbstractAnAction;

public class LangSearchAction extends AbstractAnAction {

    @Override
    public void doAction(AnActionEvent event) {
        LangSearchDlg dialog = new LangSearchDlg(event);
        dialog.show();
    }

}
