package com.tanner.module;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.tanner.base.BusinessException;
import com.tanner.base.ConfigureFileUtil;
import com.tanner.base.ProjectManager;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.text.MessageFormat;

public class NewComponentDialog extends DialogWrapper {

    private final AnActionEvent event;
    private JPanel contentPane;
    private JTextField displayText;
    private JTextField nameText;

    public NewComponentDialog(final AnActionEvent event) {
        super(event.getProject());
        init();
        this.event = event;
        Project project = event.getProject();
        setTitle("Creat New Uap Componet...");
        setSize(900, 300);
    }

    @Override
    protected void doOKAction() {
        String name = nameText.getText();
        if (StringUtils.isBlank(name)) {
            Messages.showErrorDialog("Please set componet name!", "Error");
            return;
        }
        String display = displayText.getText();
        if (StringUtils.isBlank(display)) {
            Messages.showErrorDialog("Please set componet display!", "Error");
            return;
        }
        if (!name.matches("[a-zA-Z]+")) {
            Messages.showErrorDialog("The name must be using letter only!", "Error");
            return;
        }
        if (!display.matches("[a-zA-Z]+")) {
            Messages.showErrorDialog("The display must be using letter only!", "Error");
            return;
        }
        String modulePath = event.getData(CommonDataKeys.VIRTUAL_FILE).getPath();
        File file = new File(modulePath + File.separator + name);
        if (file.exists()) {
            Messages.showErrorDialog("Componet is exists! please replace name !", "Error");
            return;
        }
        String[] dirs = new String[]{"META-INF", "METADATA", "resources", "src/public", "src/private",
                "src/client", "script/conf", "config"};
        ConfigureFileUtil util = new ConfigureFileUtil();
        File manifest = new File(modulePath + File.separator + "manifest.xml");
        String oldManifest = null;
        try {
            if (manifest.exists()) {
                oldManifest = util.readTemplate(manifest);
            }
            String template = util.readTemplate("component.xml");
            String componentContent = MessageFormat.format(template, name, display);
            String newManifest;
            if (oldManifest != null) {
                template = util.readTemplate("BusinessComponet.xml");
                String content = MessageFormat.format(template, name, display)
                        .replace("<Manifest>", "");
                newManifest = oldManifest.replace("</Manifest>", content);
            } else {
                template = util.readTemplate("manifest.xml");
                newManifest = MessageFormat.format(template, name, display);
            }
            for (String dir : dirs) {
                String path = file.getPath() + File.separator + dir;
                if (!new File(path).mkdirs() && !new File(path).isDirectory()) {
                    throw new BusinessException("无法创建组件目录: " + path);
                }
            }
            util.outFile(new File(file.getPath() + File.separator + "component.xml"),
                    componentContent, "utf-8", false);
            util.outFile(manifest, newManifest, "utf-8", false);
            //添加source目录
            Module module = ProjectManager.getInstance().getModule(event.getProject(),
                    file.getParentFile().getName());
            if (module == null) {
                throw new BusinessException("Can't find module: "
                        + file.getParentFile().getName());
            }
            ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(module)
                    .getModifiableModel();
            ContentEntry[] contentEntries = modifiableModel.getContentEntries();
            if (contentEntries.length == 0) {
                modifiableModel.dispose();
                throw new BusinessException("Module has no content root: " + module.getName());
            }
            ContentEntry contentEntry = contentEntries[0];
            for (String str : dirs) {
                if (str.contains("src")) {
                    VirtualFile sourceRoot = LocalFileSystem.getInstance().refreshAndFindFileByPath(
                            FileUtil.toSystemIndependentName(file.getPath() + File.separator + str));
                    if (sourceRoot == null) {
                        modifiableModel.dispose();
                        throw new BusinessException("Can't create source root: " + str);
                    }
                    contentEntry.addSourceFolder(sourceRoot, false);
                }
            }
            Application applicationManager = ApplicationManager.getApplication();
            applicationManager.runWriteAction(modifiableModel::commit);
            close(0);
        } catch (Exception e) {
            FileUtil.delete(file);
            try {
                if (oldManifest == null) {
                    FileUtil.delete(manifest);
                } else {
                    util.outFile(manifest, oldManifest, "utf-8", false);
                }
            } catch (Exception ignored) {
                // 原始异常更能说明创建失败原因。
            }
            Messages.showErrorDialog(e.getMessage(), "Error");
        }
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return contentPane;
    }

}
