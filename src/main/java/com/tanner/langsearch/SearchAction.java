package com.tanner.langsearch;

import com.intellij.openapi.ui.Messages;
import com.tanner.abs.AbstractButtonAction;
import com.tanner.abs.AbstractDialog;
import com.tanner.base.BusinessException;
import com.tanner.base.UapProjectEnvironment;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.io.File;
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
        String homePath = UapProjectEnvironment.getInstance().getUapHomePath();
        List<LangInfo> langInfos = readLangFromJar(homePath, searchText);
        for (LangInfo langInfo : langInfos) {
            Vector<Object> rowData = new Vector<>();
            rowData.add(langInfos.indexOf(langInfo) + 1);
            rowData.add(langInfo.getLineNumber());
            rowData.add(langInfo.getLanguage());
            rowData.add(langInfo.getText());
            rowData.add(langInfo.getPath());
            rowData.add(langInfo.getInternalPath());
            ((DefaultTableModel) searchResultTable.getModel()).addRow(rowData);
        }
    }

    private String getLanguage(String path, String text) {
        if (path.contains("simpchn") || text.contains("simpchn")) {
            return "简体中文";
        } else if (path.contains("english") || text.contains("english")) {
            return "英文";
        }
        return "-";
    }

    private List<LangInfo> readLangFromJar(String homePath, String searchValue) {
        String langLibPath = homePath + File.separator + "langlib";
        Collection<File> jarFiles = FileUtils.listFiles(new File(langLibPath), new String[]{"jar"}, true);
        List<LangInfo> matchedLangs = new ArrayList<>();
        for (File file : jarFiles) {
            try {
                JarFile jarFile = new JarFile(file);
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (entry.getName().endsWith(".properties")) {
                        InputStream is = jarFile.getInputStream(entry);
                        List<String> lines = IOUtils.readLines(is, StandardCharsets.UTF_16BE);
                        lines.stream().filter(line -> line.contains(searchValue))
                                .forEach(line -> {
                                    int lineNumber = lines.indexOf(line) + 1;
                                    String path = file.getPath();
                                    String language = getLanguage(path, entry.getName());
                                    matchedLangs.add(new LangInfo(lineNumber, path, entry.getName(), language, line));
                                });

                        is.close();
                    }
                }
                jarFile.close();
            } catch (Exception ignored) {

            }
        }
        return matchedLangs;
    }
}