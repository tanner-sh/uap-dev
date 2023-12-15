package com.tanner.module;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.tanner.base.BusinessException;
import com.tanner.base.ConfigureFileUtil;
import com.tanner.base.ProjectManager;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.MessageFormat;

public class NewModuleDialog extends DialogWrapper {

    private JPanel contentPane;

    private JTextField location;
    private JButton locationFileChooseBtn;
    private JTextField moduleNameField;
    private JTextField ncModuleNameField;
    private AnActionEvent event;
    private String modulePath;

    public NewModuleDialog(final AnActionEvent event) {
        super(event.getProject());
        init();
        setTitle("Creat New Uap Module...");
        setSize(900, 300);
        this.event = event;
        if (event.getData(CommonDataKeys.VIRTUAL_FILE) == null) {
            modulePath = event.getProject().getBasePath();
        } else {
            modulePath = event.getData(CommonDataKeys.VIRTUAL_FILE).getPath();
        }
        location.setText(modulePath);
        // 保存路径按钮事件
        locationFileChooseBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser(modulePath);
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int flag = fileChooser.showOpenDialog(null);
                if (flag == JFileChooser.APPROVE_OPTION) {
                    location.setText(fileChooser.getSelectedFile().getAbsolutePath());
                }
            }
        });
    }

    @Override
    protected void doOKAction() {
        String moduleNameText = moduleNameField.getText();
        String ncModuleNameText = ncModuleNameField.getText();
        if (StringUtils.isBlank(moduleNameText)) {
            Messages.showErrorDialog("Please set module name!", "Error");
            return;
        }
        if (StringUtils.isBlank(ncModuleNameText)) {
            Messages.showErrorDialog("Please set NC Module name!", "Error");
            return;
        }
        String locationText = location.getText();
        if (StringUtils.isBlank(locationText)) {
            Messages.showErrorDialog("Please set Module file location !", "Error");
            return;
        }
        Project project = event.getProject();
        try {
            //创建module
            UapModuleBuilder builder = new UapModuleType().createModuleBuilder();
            String modulePath = locationText + File.separator + moduleNameText;
            builder.setModuleFilePath(modulePath + File.separator + moduleNameText + ".iml");
            builder.setContentEntryPath(modulePath);
            builder.setName(moduleNameText);
            Module module = builder.commitModule(project, null);
            //输出配置文件
            ConfigureFileUtil util = new ConfigureFileUtil();
            String meatPath = modulePath + File.separator + "META-INF";
            new File(meatPath).mkdirs();
            File file = new File(meatPath + File.separator + "module.xml");
            String template = util.readTemplate("module.xml");
            String content = MessageFormat.format(template, ncModuleNameText);
            util.outFile(file, content, "gb2312", false);
            //设置类路径
            ProjectManager.getInstance().setModuleLibrary(project, module);
        } catch (BusinessException e) {
            Messages.showErrorDialog(e.getMessage(), "Error");
        }
        close(0);
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return contentPane;
    }

}
