package com.tanner.langsearch;

import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.tanner.abs.AbstractButtonAction;
import com.tanner.abs.AbstractDialog;
import com.tanner.base.BusinessException;
import com.tanner.base.UapProjectEnvironment;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class SearchAction extends AbstractButtonAction {

    public SearchAction(AbstractDialog dialog) {
        super(dialog);
    }

    @Override
    public void doAction(ActionEvent event) throws BusinessException {
        JTextField searchTextField = getDialog().getComponent(JTextField.class, "searchTextField");
        String searchText = searchTextField.getText();
        if (StringUtils.isBlank(searchText)) {
            Messages.showInfoMessage("Text can not be null!", "提示");
            return;
        }
        JTable searchResultTable = getDialog().getComponent(JTable.class, "searchResultTable");
        for (int rowCount = searchResultTable.getModel().getRowCount(); rowCount > 0; rowCount--) {
            ((DefaultTableModel) searchResultTable.getModel()).removeRow(rowCount - 1);
        }
        String homePath = UapProjectEnvironment.getInstance(
                getDialog().getProjectContext()).getUapHomePath();
        Project project = getDialog().getProjectContext();
        JButton searchButton = getDialog().getComponent(JButton.class, "searchBtn");
        searchButton.setEnabled(false);
        Task.Backgroundable task = new Task.Backgroundable(project, "Searching language files...",
                true) {
            private List<LangInfo> result = List.of();
            private Exception failure;

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    indicator.setIndeterminate(true);
                    result = readLangFromJar(homePath, searchText, indicator);
                } catch (ProcessCanceledException exception) {
                    throw exception;
                } catch (Exception exception) {
                    failure = exception;
                }
            }

            @Override
            public void onSuccess() {
                searchButton.setEnabled(true);
                if (failure != null) {
                    Messages.showErrorDialog(failure.getMessage(), "错误");
                    return;
                }
                for (int i = 0; i < result.size(); i++) {
                    LangInfo langInfo = result.get(i);
                    Vector<Object> rowData = new Vector<>();
                    rowData.add(i + 1);
                    rowData.add(langInfo.getLineNumber());
                    rowData.add(langInfo.getLanguage());
                    rowData.add(langInfo.getText());
                    rowData.add(langInfo.getPath());
                    rowData.add(langInfo.getInternalPath());
                    ((DefaultTableModel) searchResultTable.getModel()).addRow(rowData);
                }
            }

            @Override
            public void onCancel() {
                searchButton.setEnabled(true);
            }
        };
        ProgressManager.getInstance().run(task);
    }

    private String getLanguage(String path, String text) {
        if (path.contains("simpchn") || text.contains("simpchn")) {
            return "简体中文";
        } else if (path.contains("english") || text.contains("english")) {
            return "英文";
        }
        return "-";
    }

    private List<LangInfo> readLangFromJar(String homePath, String searchValue,
                                           ProgressIndicator indicator) {
        String langLibPath = homePath + File.separator + "langlib";
        Collection<File> jarFiles = FileUtils.listFiles(new File(langLibPath), new String[]{"jar"}, true);
        List<LangInfo> matchedLangs = new ArrayList<>();
        for (File file : jarFiles) {
            indicator.checkCanceled();
            try (JarFile jarFile = new JarFile(file)) {
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    indicator.checkCanceled();
                    JarEntry entry = entries.nextElement();
                    if (entry.getName().endsWith(".properties")) {
                        try (InputStream is = jarFile.getInputStream(entry)) {
                            List<String> lines = IOUtils.readLines(is, StandardCharsets.UTF_16BE);
                            for (int i = 0; i < lines.size(); i++) {
                                String line = lines.get(i);
                                if (line.contains(searchValue)) {
                                    String path = file.getPath();
                                    String language = getLanguage(path, entry.getName());
                                    matchedLangs.add(new LangInfo(i + 1, path, entry.getName(),
                                            language, line));
                                }
                            }
                        }
                    }
                }
            } catch (IOException ignored) {

            }
        }
        return matchedLangs;
    }
}
