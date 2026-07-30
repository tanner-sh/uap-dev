package com.tanner.patcher.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.tanner.abs.AbstractDialog;
import com.tanner.base.UapProjectEnvironment;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.util.Objects;

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
    private JTextField uapVersion;
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
        String version = null;
        if (envSettingService != null) {
            lastPatcherPath = envSettingService.getLastPatcherPath();
            userName = envSettingService.getDeveloper();
            version = envSettingService.getUapVersion();
        }
        if (StringUtils.isEmpty(lastPatcherPath) || !new File(lastPatcherPath).exists()) {
            lastPatcherPath = System.getProperty("user.home");
        }
        savePath.setText(lastPatcherPath);
        if (StringUtils.isEmpty(userName)) {
            userName = System.getProperties().getProperty("user.name", "unknown");
        }
        developer.setText(userName);
        uapVersion.setText(version);
        if (Objects.equals(version, "fbip81")) {
            serverName.setText("fbip");
        }
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
        if (null == uapVersion.getText() || "".equals(uapVersion.getText())) {
            Messages.showErrorDialog("Please Set uapVersion!", "Error");
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
            envSettingService.setUapVersion(uapVersion.getText());
        }
        boolean srcFlag = srcFlagCheckBox.isSelected();
        boolean cloudFlag = cloudFlagCheckBox.isSelected();
        boolean needDeployFlag = needDeploy.isSelected();
        boolean needClearSwingCacheFlag = needClearSwingCache.isSelected();
        boolean needClearBrowserCacheFlag = needClearBrowserCache.isSelected();
        final String patchName = patcherName.getText().trim();
        final String developerName = developer.getText().trim();
        final String versionText = uapVersion.getText().trim();
        try {
            ExportPatcherUtil.validateFileNamePart(patchName, "patcher name");
            ExportPatcherUtil.validateFileNamePart(developerName, "developer");
            ExportPatcherUtil.validateFileNamePart(versionText, "uapVersion");
        } catch (Exception exception) {
            Messages.showErrorDialog(exception.getMessage(), "Error");
            return;
        }
        final Project project = event.getProject();
        final VirtualFile[] selectedFiles = event.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
        final String serverNameText = serverName.getText();
        final String projectNameText = projectName.getText();
        final String functionText = functionDescription.getText();
        final String configText = configDescription.getText();
        logPanel.setVisible(true);
        progressBar.setIndeterminate(true);
        setOKActionEnabled(false);
        Task.Backgroundable task = new Task.Backgroundable(project, "Exporting UAP patch...", true) {
            private ExportPatcherUtil util;
            private Exception failure;

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    util = new ExportPatcherUtil(project, selectedFiles, exportPath, patchName,
                            srcFlag, serverNameText, cloudFlag, projectNameText, needDeployFlag,
                            needClearSwingCacheFlag, needClearBrowserCacheFlag, functionText,
                            configText, developerName, versionText);
                    util.exportPatcher(indicator);
                } catch (ProcessCanceledException exception) {
                    throw exception;
                } catch (Exception exception) {
                    failure = exception;
                } finally {
                    if (util != null) {
                        util.delete(new File(util.getExportPath()));
                    }
                }
            }

            @Override
            public void onSuccess() {
                progressBar.setIndeterminate(false);
                setOKActionEnabled(true);
                if (failure != null) {
                    Messages.showErrorDialog(failure.getMessage(), "Error");
                    return;
                }
                String zipName = util == null ? "" : util.getZipName();
                if (StringUtils.isBlank(zipName)) {
                    zipName = "no files export , please build project , or select src retry !";
                } else {
                    zipName = "outFile : " + zipName;
                }
                Messages.showInfoMessage("Success!\n" + zipName, "Tips");
                dispose();
            }

            @Override
            public void onCancel() {
                progressBar.setIndeterminate(false);
                setOKActionEnabled(true);
            }
        };
        ProgressManager.getInstance().run(task);
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
