package com.tanner.devconfig.action.button;

import com.intellij.openapi.ui.Messages;
import com.tanner.abs.AbstractButtonAction;
import com.tanner.abs.AbstractDialog;
import com.tanner.base.BusinessException;
import com.tanner.devconfig.DevConfigDialog;
import com.tanner.library.action.LibrariesUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class SetLibraryAction extends AbstractButtonAction {

    public SetLibraryAction(AbstractDialog dialog) {
        super(dialog);
    }

    @Override
    public void doAction(ActionEvent event) throws BusinessException {
        DevConfigDialog dialog = (DevConfigDialog) getDialog();
        String homePath = dialog.getComponent(JTextField.class, "homeText").getText();
        LibrariesUtil.setLibraries(homePath);
        Messages.showInfoMessage("Set done！", "提示");
        dialog.setLibFlag(true);
    }
}
