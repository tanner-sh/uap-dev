package com.tanner.langsearch;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
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
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class SearchAction extends AbstractButtonAction {

    private static final Logger LOG = Logger.getInstance(SearchAction.class);

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
            private final List<String> warnings = new ArrayList<>();
            private Exception failure;

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    indicator.setIndeterminate(true);
                    result = readLangFromJar(homePath, searchText, indicator, warnings);
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
                if (!warnings.isEmpty()) {
                    String details = String.join("\n", warnings.subList(0,
                            Math.min(warnings.size(), 5)));
                    if (warnings.size() > 5) {
                        details += "\n...另有 " + (warnings.size() - 5) + " 个文件";
                    }
                    Messages.showWarningDialog("部分语言包读取失败：\n" + details, "警告");
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
                                           ProgressIndicator indicator,
                                           List<String> warnings) {
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
                            List<LineMatch> matches = findMatchingLines(
                                    IOUtils.toByteArray(is), searchValue);
                            for (LineMatch match : matches) {
                                String path = file.getPath();
                                String language = getLanguage(path, entry.getName());
                                matchedLangs.add(new LangInfo(match.lineNumber(), path,
                                        entry.getName(), language, match.text()));
                            }
                        }
                    }
                }
            } catch (IOException exception) {
                String warning = file.getPath() + ": " + exception.getMessage();
                warnings.add(warning);
                LOG.warn("Failed to read language JAR: " + file, exception);
            }
        }
        return matchedLangs;
    }

    static List<LineMatch> findMatchingLines(byte[] content, String searchValue) {
        for (Charset charset : candidateCharsets(content)) {
            try {
                String text = charset.newDecoder()
                        .onMalformedInput(CodingErrorAction.REPORT)
                        .onUnmappableCharacter(CodingErrorAction.REPORT)
                        .decode(ByteBuffer.wrap(content)).toString();
                if (!text.isEmpty() && text.charAt(0) == '\uFEFF') {
                    text = text.substring(1);
                }
                String[] lines = text.split("\\R", -1);
                List<LineMatch> matches = new ArrayList<>();
                for (int i = 0; i < lines.length; i++) {
                    if (lines[i].contains(searchValue)) {
                        matches.add(new LineMatch(i + 1, lines[i]));
                    }
                }
                if (!matches.isEmpty()) {
                    return matches;
                }
            } catch (CharacterCodingException ignored) {
                // Try the next supported language-pack encoding.
            }
        }
        return List.of();
    }

    private static Set<Charset> candidateCharsets(byte[] content) {
        Set<Charset> charsets = new LinkedHashSet<>();
        if (startsWith(content, (byte) 0xFE, (byte) 0xFF)) {
            charsets.add(StandardCharsets.UTF_16BE);
        } else if (startsWith(content, (byte) 0xFF, (byte) 0xFE)) {
            charsets.add(StandardCharsets.UTF_16LE);
        } else if (startsWith(content, (byte) 0xEF, (byte) 0xBB, (byte) 0xBF)) {
            charsets.add(StandardCharsets.UTF_8);
        }
        // UTF-16BE is the historical NC language-pack format.
        charsets.add(StandardCharsets.UTF_16BE);
        charsets.add(StandardCharsets.UTF_16LE);
        charsets.add(StandardCharsets.UTF_8);
        charsets.add(Charset.forName("GB18030"));
        charsets.add(StandardCharsets.ISO_8859_1);
        return charsets;
    }

    private static boolean startsWith(byte[] content, byte... prefix) {
        return content.length >= prefix.length
                && Arrays.equals(Arrays.copyOf(content, prefix.length), prefix);
    }

    record LineMatch(int lineNumber, String text) {
    }
}
