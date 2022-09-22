package com.tanner.script.export.action;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.tanner.abs.AbstractButtonAction;
import com.tanner.abs.AbstractDialog;
import com.tanner.base.ProjectManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class PathSelAction extends AbstractButtonAction {


    public PathSelAction(AbstractDialog dialog) {
        super(dialog);
    }

    @Override
    public void doAction(ActionEvent event) {
        JTextField exportPathText = getDialog().getComponent(JTextField.class, "exportPathText");
        String exportPath = exportPathText.getText();
        FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        VirtualFile virtualFile = FileChooser.chooseFile(descriptor,
                ProjectManager.getInstance().getProject(),
                LocalFileSystem.getInstance().findFileByIoFile(new File(exportPath)));
        if (virtualFile == null) {
            return;
        }
        exportPathText.setText(virtualFile.getPath());
    }

}
