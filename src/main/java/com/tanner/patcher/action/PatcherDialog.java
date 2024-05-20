package com.tanner.patcher.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.tanner.abs.AbstractDialog;
import com.tanner.base.UapProjectEnvironment;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class PatcherDialog extends AbstractDialog {

    private final AnActionEvent event;

    private JPanel contentPane;
    private JTextField savePath;
    private JButton fileChooseBtn;
    private JPanel filePanel;
    private JTextField patcherName;
    private JTextField serverName;
    private JCheckBox srcFlagCheckBox;
    private JProgressBar progressBar;
    private JPanel logPanel;
    private JCheckBox cloudFlagCheckBox;
    private JTextField projectName;
    private JCheckBox needDeploy;
    private JCheckBox needClearSwingCache;
    private JCheckBox needClearBrowserCache;
    private JTextArea functionDescription;
    private JTextArea configDescription;
    private JTextField developer;
    private JBList<VirtualFile> fieldList;

    public PatcherDialog(final AnActionEvent event) {
        super(event.getProject());
        this.event = event;
        init();
        setSize(900, 600);
        setTitle("Export Uap Patcher...");
        logPanel.setVisible(false);
        patcherName.setEditable(true);
        UapProjectEnvironment envSettingService = UapProjectEnvironment.getInstance(event.getProject());
        String lastPatcherPath = null;
        String userName = null;
        if (envSettingService != null) {
            lastPatcherPath = envSettingService.getLastPatcherPath();
            userName = envSettingService.getDeveloper();
        }
        if (StringUtils.isEmpty(lastPatcherPath) || !new File(lastPatcherPath).exists()) {
            lastPatcherPath = System.getProperty("user.home");
        }
        savePath.setText(lastPatcherPath);
        if (StringUtils.isEmpty(userName)) {
            userName = System.getProperties().getProperty("user.name", "unknown");

        }
        developer.setText(userName);
        // 保存路径按钮事件
        fileChooseBtn.addActionListener(e -> {
            FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
            VirtualFile virtualFile = FileChooser.chooseFile(descriptor, event.getProject(),
                    LocalFileSystem.getInstance().findFileByIoFile(new File(savePath.getText())));
            if (virtualFile != null) {
                savePath.setText(virtualFile.getPath());
            }
        });
    }

    @Override
    protected void doOKAction() {
        // 条件校验
        if (null == patcherName.getText() || "".equals(patcherName.getText())) {
            Messages.showErrorDialog("Please set patcher name!", "Error");
            return;
        }
        if (null == savePath.getText() || "".equals(savePath.getText())) {
            Messages.showErrorDialog("Please select save path!", "Error");
            return;
        }
        if (null == developer.getText() || "".equals(developer.getText())) {
            Messages.showErrorDialog("Please Set developer!", "Error");
            return;
        }
        ListModel<VirtualFile> model = fieldList.getModel();
        if (model.getSize() == 0) {
            Messages.showErrorDialog("Please select export file!", "Error");
            return;
        }
        String exportPath = savePath.getText();
        UapProjectEnvironment envSettingService = UapProjectEnvironment.getInstance(event.getProject());
        if (envSettingService != null) {
            envSettingService.setLastPatcherPath(exportPath);
            envSettingService.setDeveloper(developer.getText());
        }
        boolean srcFlag = srcFlagCheckBox.isSelected();
        boolean cloudFlag = cloudFlagCheckBox.isSelected();
        boolean needDeployFlag = needDeploy.isSelected();
        boolean needClearSwingCacheFlag = needClearSwingCache.isSelected();
        boolean needClearBrowserCacheFlag = needClearBrowserCache.isSelected();
        // 设置当前进度值
        logPanel.setVisible(true);
        progressBar.setValue(0);
        // 绘制百分比文本（进度条中间显示的百分数）
        progressBar.setStringPainted(true);
        progressBar.addChangeListener(e -> {
            Dimension dimension = progressBar.getSize();
            Rectangle rect = new Rectangle(0, 0, dimension.width, dimension.height);
            progressBar.paintImmediately(rect);
        });
        SwingUtilities.invokeLater(() -> {
            ExportPatcherUtil util = new ExportPatcherUtil(event, exportPath, patcherName.getText(),
                    srcFlag, serverName.getText(), cloudFlag, projectName.getText(), needDeployFlag,
                    needClearSwingCacheFlag, needClearBrowserCacheFlag, functionDescription.getText(),
                    configDescription.getText(), developer.getText());
            try {
                util.exportPatcher(progressBar);
                String zipName = util.getZipName();
                if (StringUtils.isBlank(zipName)) {
                    zipName = "no files export , please build project , or select src retry !";
                } else {
                    zipName = "outFile : " + zipName;
                }
                Messages.showInfoMessage("Success!\n" + zipName, "Tips");
                dispose();
            } catch (Exception e) {
                Messages.showErrorDialog(e.getMessage(), "Error");
            } finally {
                util.delete(new File(util.getExportPath()));
                dispose();
            }
        });
    }

    private void createUIComponents() {
        VirtualFile[] data = event.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
        assert data != null;
        fieldList = new JBList<>(data);
        fieldList.setEmptyText("No file selected!");
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(fieldList);
        filePanel = decorator.createPanel();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return contentPane;
    }

}
