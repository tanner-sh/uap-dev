package com.tanner.devconfig.action.button;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.tanner.abs.AbstractButtonAction;
import com.tanner.base.ProjectManager;
import com.tanner.base.UapProjectEnvironment;
import com.tanner.devconfig.DevConfigDialog;
import com.tanner.devconfig.util.DataSourceUtil;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * 选择home
 */
public class SelHomePathAction extends AbstractButtonAction {

    public SelHomePathAction(DevConfigDialog dialog) {
        super(dialog);
    }

    @Override
    public void doAction(ActionEvent event) {
        String ncHome = UapProjectEnvironment.getInstance().getUapHomePath();
        Project project = ProjectManager.getInstance().getProject();
        if (StringUtils.isBlank(ncHome)) {
            ncHome = project.getBasePath();
        }
        FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        VirtualFile virtualFile = FileChooser.chooseFile(descriptor, project,
                LocalFileSystem.getInstance().findFileByIoFile(new File(ncHome)));
        if (virtualFile == null) {
            return;
        }
        //设置文本框显示
        getDialog().getComponent(JTextField.class, "homeText").setText(virtualFile.getPath());
        //根据最新路径重新加载数据源
        DataSourceUtil.initDataSource((DevConfigDialog) getDialog());
    }
}
