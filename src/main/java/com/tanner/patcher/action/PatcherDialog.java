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
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

public class PatcherDialog extends AbstractDialog {

    private final AnActionEvent event;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
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
    private JBList<VirtualFile> fieldList;

    public PatcherDialog(final AnActionEvent event) {
        this.event = event;
        setTitle("export uap patcher...");
        logPanel.setVisible(false);
        patcherName.setEditable(true);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());
        addWindowListener(new MyWindowAdapter());
        contentPane.registerKeyboardAction(e -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        UapProjectEnvironment envSettingService = UapProjectEnvironment.getInstance(event.getProject());
        String lastPatcherPath = envSettingService.getLastPatcherPath();
        if (StringUtils.isEmpty(lastPatcherPath) || !new File(lastPatcherPath).exists()) {
            lastPatcherPath = System.getProperty("user.home");
        }
        savePath.setText(lastPatcherPath);
        // ????????????????????????
        fileChooseBtn.addActionListener(e -> {
            FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
            VirtualFile virtualFile = FileChooser.chooseFile(descriptor, event.getProject(),
                    LocalFileSystem.getInstance().findFileByIoFile(new File(savePath.getText())));
            if (virtualFile != null) {
                savePath.setText(virtualFile.getPath());
            }
        });
    }

    private void onOK() {
        // ????????????
        if (null == patcherName.getText() || "".equals(patcherName.getText())) {
            Messages.showErrorDialog(this, "Please set patcher name!", "Error");
            return;
        }
        if (null == savePath.getText() || "".equals(savePath.getText())) {
            Messages.showErrorDialog(this, "Please Select Save Path!", "Error");
            return;
        }
        ListModel<VirtualFile> model = fieldList.getModel();
        if (model.getSize() == 0) {
            Messages.showErrorDialog(this, "Please Select Export File!", "Error");
            return;
        }
        String exportPath = savePath.getText();
        UapProjectEnvironment envSettingService = UapProjectEnvironment.getInstance(event.getProject());
        if (envSettingService != null) {
            envSettingService.setLastPatcherPath(exportPath);
        }
        boolean srcFlag = srcFlagCheckBox.isSelected();
        boolean cloudFlag = cloudFlagCheckBox.isSelected();
        boolean needDeployFlag = needDeploy.isSelected();
        boolean needClearSwingCacheFlag = needClearSwingCache.isSelected();
        boolean needClearBrowserCacheFlag = needClearBrowserCache.isSelected();
        // ?????????????????????
        logPanel.setVisible(true);
        progressBar.setValue(0);
        // ????????????????????????????????????????????????????????????
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
                    configDescription.getText());
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

    private void onCancel() {
        dispose();
    }

    private void createUIComponents() {
        VirtualFile[] data = event.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
        assert data != null;
        fieldList = new JBList<VirtualFile>(data);
        fieldList.setEmptyText("No File Selected!");
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(fieldList);
        filePanel = decorator.createPanel();
    }

    private class MyWindowAdapter extends WindowAdapter {

        @Override
        public void windowClosing(WindowEvent e) {
            onCancel();
        }
    }
}
