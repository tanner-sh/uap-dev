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
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.TitledSeparator;
import com.intellij.util.ui.JBUI;
import com.tanner.abs.AbstractDialog;
import com.tanner.base.UapProjectEnvironment;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PatcherDialog extends AbstractDialog {

    private final AnActionEvent event;
    private final JPanel contentPane = new JPanel(new BorderLayout(0, JBUI.scale(8)));
    private final JBTextField savePath = new JBTextField();
    private final JButton fileChooseBtn = new JButton("选择…");
    private JPanel filePanel;
    private final JBTextField patcherName = new JBTextField();
    private final JBTextField serverName = new JBTextField("nccloud");
    private final JCheckBox srcFlagCheckBox = new JCheckBox("包含源文件", true);
    private final JProgressBar progressBar = new JProgressBar();
    private final JPanel logPanel = new JPanel(new BorderLayout(JBUI.scale(8), 0));
    private final JCheckBox cloudFlagCheckBox = new JCheckBox("Cloud 模式");
    private final JBTextField projectName = new JBTextField();
    private final JCheckBox needDeploy = new JCheckBox("生成部署信息");
    private final JCheckBox needClearSwingCache = new JCheckBox("清理 Swing 缓存");
    private final JCheckBox needClearBrowserCache = new JCheckBox("清理浏览器缓存");
    private final JBTextArea functionDescription = new JBTextArea(3, 20);
    private final JBTextArea configDescription = new JBTextArea(3, 20);
    private final JBTextField developer = new JBTextField();
    private final JBTextField uapVersion = new JBTextField();
    private final JBList<VirtualFile> fieldList;
    private final JBLabel statusLabel = new JBLabel("准备导出");
    private final DefaultListModel<VirtualFile> fileListModel = new DefaultListModel<>();

    public PatcherDialog(AnActionEvent event) {
        super(event.getProject());
        this.event = event;
        VirtualFile[] selectedFiles = event.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
        if (selectedFiles != null) {
            for (VirtualFile selectedFile : selectedFiles) {
                fileListModel.addElement(selectedFile);
            }
        }
        fieldList = new JBList<>(fileListModel);
        buildUi();
        setTitle("导出 UAP 补丁");
        setResizable(true);
        init();
        getOKAction().putValue(Action.NAME, "导出");
        getCancelAction().putValue(Action.NAME, "取消");
        loadDefaults();
        initListeners();
    }

    private void buildUi() {
        contentPane.setBorder(JBUI.Borders.empty(8));
        contentPane.setPreferredSize(JBUI.size(900, 600));
        savePath.setEditable(false);
        configDescription.setText("不需要配置");

        JPanel pathRow = new JPanel(new BorderLayout(JBUI.scale(8), 0));
        pathRow.add(savePath, BorderLayout.CENTER);
        pathRow.add(fileChooseBtn, BorderLayout.EAST);
        JPanel basicFields = new JPanel(new GridBagLayout());
        addWideRow(basicFields, 0, "保存目录", pathRow);
        addPairRow(basicFields, 1, "开发者", developer, "UAP 版本", uapVersion);
        addPairRow(basicFields, 2, "补丁名称", patcherName, "服务名称", serverName);
        addWideRow(basicFields, 3, "项目名称", projectName);
        JPanel basicPanel = createSection("基本信息", basicFields);

        JPanel optionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, JBUI.scale(12), 0));
        optionPanel.setBorder(JBUI.Borders.empty(4, 8));
        optionPanel.add(srcFlagCheckBox);
        optionPanel.add(cloudFlagCheckBox);
        optionPanel.add(needDeploy);
        optionPanel.add(needClearSwingCache);
        optionPanel.add(needClearBrowserCache);

        functionDescription.setLineWrap(true);
        functionDescription.setWrapStyleWord(true);
        configDescription.setLineWrap(true);
        configDescription.setWrapStyleWord(true);
        JPanel functionPanel = createTextAreaPanel("功能说明", functionDescription);
        JPanel configPanel = createTextAreaPanel("配置说明", configDescription);
        JPanel descriptionFields = new JPanel(new GridLayout(1, 2, JBUI.scale(8), 0));
        descriptionFields.add(functionPanel);
        descriptionFields.add(configPanel);
        descriptionFields.setPreferredSize(JBUI.size(0, 140));
        JPanel descriptionPanel = createSection("补丁说明", descriptionFields);

        fieldList.setEmptyText("未选择导出文件");
        fieldList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean selected, boolean focused) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, selected, focused);
                if (value instanceof VirtualFile file) {
                    label.setText(file.getPath());
                }
                return label;
            }
        });
        JPanel decoratedList = ToolbarDecorator.createDecorator(fieldList)
                .disableAddAction()
                .disableUpDownActions()
                .setRemoveAction(button -> removeSelectedFiles())
                .createPanel();
        decoratedList.setBorder(JBUI.Borders.empty());
        filePanel = createSection("导出文件", decoratedList);

        JPanel upper = new JPanel();
        upper.setLayout(new BoxLayout(upper, BoxLayout.Y_AXIS));
        upper.add(basicPanel);
        upper.add(Box.createVerticalStrut(JBUI.scale(8)));
        upper.add(createSection("导出选项", optionPanel));
        JPanel details = new JPanel(new BorderLayout(0, JBUI.scale(8)));
        details.add(descriptionPanel, BorderLayout.NORTH);
        details.add(filePanel, BorderLayout.CENTER);

        logPanel.add(statusLabel, BorderLayout.WEST);
        logPanel.add(progressBar, BorderLayout.CENTER);
        logPanel.setVisible(false);
        contentPane.add(upper, BorderLayout.NORTH);
        contentPane.add(details, BorderLayout.CENTER);
        contentPane.add(logPanel, BorderLayout.SOUTH);
    }

    private static JPanel createSection(String title, JComponent content) {
        JPanel panel = new JPanel(new BorderLayout(0, JBUI.scale(4)));
        panel.add(new TitledSeparator(title), BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private static JPanel createTextAreaPanel(String label, JBTextArea textArea) {
        JPanel panel = new JPanel(new BorderLayout(0, JBUI.scale(4)));
        panel.add(new JBLabel(label + "："), BorderLayout.NORTH);
        panel.add(new JBScrollPane(textArea), BorderLayout.CENTER);
        return panel;
    }

    private static void addWideRow(JPanel panel, int row, String label, JComponent component) {
        GridBagConstraints labelConstraints = formConstraints(0, row);
        labelConstraints.anchor = GridBagConstraints.WEST;
        panel.add(new JBLabel(label + "："), labelConstraints);
        GridBagConstraints fieldConstraints = formConstraints(1, row);
        fieldConstraints.gridwidth = 3;
        fieldConstraints.weightx = 1;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(component, fieldConstraints);
    }

    private static void addPairRow(JPanel panel, int row, String leftLabel,
                                   JComponent leftComponent, String rightLabel,
                                   JComponent rightComponent) {
        GridBagConstraints leftLabelConstraints = formConstraints(0, row);
        leftLabelConstraints.anchor = GridBagConstraints.WEST;
        panel.add(new JBLabel(leftLabel + "："), leftLabelConstraints);
        GridBagConstraints leftFieldConstraints = formConstraints(1, row);
        leftFieldConstraints.weightx = 1;
        leftFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(leftComponent, leftFieldConstraints);
        GridBagConstraints rightLabelConstraints = formConstraints(2, row);
        rightLabelConstraints.anchor = GridBagConstraints.WEST;
        panel.add(new JBLabel(rightLabel + "："), rightLabelConstraints);
        GridBagConstraints rightFieldConstraints = formConstraints(3, row);
        rightFieldConstraints.weightx = 1;
        rightFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(rightComponent, rightFieldConstraints);
    }

    private static GridBagConstraints formConstraints(int column, int row) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = column;
        constraints.gridy = row;
        constraints.insets = JBUI.insets(4, 8);
        return constraints;
    }

    private void removeSelectedFiles() {
        int[] selectedIndices = fieldList.getSelectedIndices();
        for (int i = selectedIndices.length - 1; i >= 0; i--) {
            fileListModel.remove(selectedIndices[i]);
        }
    }

    private void loadDefaults() {
        UapProjectEnvironment environment = UapProjectEnvironment.getInstance(event.getProject());
        String lastPatcherPath = environment == null ? null : environment.getLastPatcherPath();
        String userName = environment == null ? null : environment.getDeveloper();
        String version = environment == null ? null : environment.getUapVersion();
        if (StringUtils.isBlank(lastPatcherPath) || !new File(lastPatcherPath).exists()) {
            lastPatcherPath = System.getProperty("user.home");
        }
        if (StringUtils.isBlank(userName)) {
            userName = System.getProperty("user.name", "unknown");
        }
        savePath.setText(lastPatcherPath);
        developer.setText(userName);
        uapVersion.setText(version);
        if (Objects.equals(version, "fbip81")) {
            serverName.setText("fbip");
        }
    }

    private void initListeners() {
        fileChooseBtn.addActionListener(action -> {
            FileChooserDescriptor descriptor =
                    FileChooserDescriptorFactory.createSingleFolderDescriptor();
            VirtualFile initial = LocalFileSystem.getInstance()
                    .findFileByIoFile(new File(savePath.getText()));
            VirtualFile selected = FileChooser.chooseFile(
                    descriptor, event.getProject(), initial);
            if (selected != null) {
                savePath.setText(selected.getPath());
            }
        });
    }

    @Override
    protected void doOKAction() {
        if (StringUtils.isBlank(patcherName.getText())) {
            Messages.showErrorDialog("请输入补丁名称", "错误");
            return;
        }
        if (StringUtils.isBlank(savePath.getText())) {
            Messages.showErrorDialog("请选择保存目录", "错误");
            return;
        }
        if (StringUtils.isBlank(developer.getText())) {
            Messages.showErrorDialog("请输入开发者", "错误");
            return;
        }
        if (StringUtils.isBlank(uapVersion.getText())) {
            Messages.showErrorDialog("请输入 UAP 版本", "错误");
            return;
        }
        ListModel<VirtualFile> model = fieldList.getModel();
        if (model.getSize() == 0) {
            Messages.showErrorDialog("请至少选择一个导出文件", "错误");
            return;
        }

        String exportPath = savePath.getText();
        String patchName = patcherName.getText().trim();
        String developerName = developer.getText().trim();
        String versionText = uapVersion.getText().trim();
        try {
            ExportPatcherUtil.validateFileNamePart(patchName, "补丁名称");
            ExportPatcherUtil.validateFileNamePart(developerName, "开发者");
            ExportPatcherUtil.validateFileNamePart(versionText, "UAP 版本");
        } catch (Exception exception) {
            Messages.showErrorDialog(exception.getMessage(), "错误");
            return;
        }
        UapProjectEnvironment environment = UapProjectEnvironment.getInstance(event.getProject());
        if (environment != null) {
            environment.setLastPatcherPath(exportPath);
            environment.setDeveloper(developerName);
            environment.setUapVersion(versionText);
        }

        List<VirtualFile> currentSelection = new ArrayList<>();
        for (int i = 0; i < model.getSize(); i++) {
            VirtualFile file = model.getElementAt(i);
            if (file != null && file.isValid()) {
                currentSelection.add(file);
            }
        }
        VirtualFile[] selectedFiles = currentSelection.toArray(new VirtualFile[0]);
        Project project = event.getProject();
        boolean includeSource = srcFlagCheckBox.isSelected();
        boolean cloudMode = cloudFlagCheckBox.isSelected();
        boolean deploy = needDeploy.isSelected();
        boolean clearSwingCache = needClearSwingCache.isSelected();
        boolean clearBrowserCache = needClearBrowserCache.isSelected();
        String serverNameText = serverName.getText();
        String projectNameText = projectName.getText();
        String functionText = functionDescription.getText();
        String configText = configDescription.getText();
        logPanel.setVisible(true);
        progressBar.setIndeterminate(true);
        statusLabel.setText("正在导出补丁…");
        setOKActionEnabled(false);
        Task.Backgroundable task = new Task.Backgroundable(
                project, "正在导出 UAP 补丁…", true) {
            private ExportPatcherUtil util;
            private Exception failure;

            private boolean isUnavailable() {
                return isDialogDisposed() || project != null && project.isDisposed();
            }

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    util = new ExportPatcherUtil(project, selectedFiles, exportPath, patchName,
                            includeSource, serverNameText, cloudMode, projectNameText,
                            deploy, clearSwingCache, clearBrowserCache, functionText,
                            configText, developerName, versionText);
                    util.exportPatcher(indicator);
                } catch (ProcessCanceledException exception) {
                    throw exception;
                } catch (Exception exception) {
                    failure = exception;
                } finally {
                    if (util != null) {
                        util.cleanupStaging();
                    }
                }
            }

            @Override
            public void onSuccess() {
                if (isUnavailable()) {
                    return;
                }
                progressBar.setIndeterminate(false);
                setOKActionEnabled(true);
                if (failure != null) {
                    statusLabel.setText("导出失败");
                    Messages.showErrorDialog(failure.getMessage(), "错误");
                    return;
                }
                String zipName = util == null ? "" : util.getZipName();
                if (StringUtils.isBlank(zipName)) {
                    statusLabel.setText("没有可导出的文件");
                    Messages.showWarningDialog(
                            "没有导出任何文件，请先构建项目或勾选包含源文件后重试。", "提示");
                    return;
                }
                statusLabel.setText("导出完成");
                Messages.showInfoMessage("补丁已导出：\n" + zipName, "完成");
                close(OK_EXIT_CODE);
            }

            @Override
            public void onCancel() {
                if (isUnavailable()) {
                    return;
                }
                progressBar.setIndeterminate(false);
                statusLabel.setText("已取消导出");
                setOKActionEnabled(true);
            }
        };
        ProgressManager.getInstance().run(task);
    }

    @Override
    protected String getDimensionServiceKey() {
        return "uap.patcher.export.dialog.v2";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return patcherName;
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return contentPane;
    }
}
