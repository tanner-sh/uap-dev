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
import com.tanner.ui.BulkTableModel;
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
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class SearchAction extends AbstractButtonAction {

    private static final Logger LOG = Logger.getInstance(SearchAction.class);

    public SearchAction(AbstractDialog dialog) {
        super(dialog);
    }

    @Override
    public void doAction(ActionEvent event) throws BusinessException {
        LangSearchDlg view = (LangSearchDlg) getDialog();
        JTextField searchTextField = view.searchTextField();
        String searchText = searchTextField.getText();
        if (StringUtils.isBlank(searchText)) {
            Messages.showInfoMessage("请输入搜索内容", "提示");
            return;
        }
        JButton searchButton = view.searchButton();
        if (!searchButton.isEnabled()) {
            return;
        }
        JTable searchResultTable = view.resultTable();
        if (searchResultTable.getModel() instanceof BulkTableModel model) {
            model.clearRows();
        } else {
            ((DefaultTableModel) searchResultTable.getModel()).setRowCount(0);
        }
        String homePath = UapProjectEnvironment.getInstance(
                getDialog().getProjectContext()).getUapHomePath();
        Project project = getDialog().getProjectContext();
        JLabel statusLabel = view.statusLabel();
        searchButton.setEnabled(false);
        searchTextField.setEnabled(false);
        if (statusLabel != null) {
            statusLabel.setText("正在扫描语言包…");
        }
        if (searchResultTable instanceof com.intellij.ui.table.JBTable jbTable) {
            jbTable.setPaintBusy(true);
        }
        Task.Backgroundable task = new Task.Backgroundable(project, "正在搜索多语文件…",
                true) {
            private List<LangInfo> result = List.of();
            private final List<String> warnings = new ArrayList<>();
            private Exception failure;

            private boolean isUnavailable() {
                return view.isDialogDisposed() || project != null && project.isDisposed();
            }

            private void restoreUi() {
                if (isUnavailable()) {
                    return;
                }
                searchButton.setEnabled(true);
                searchTextField.setEnabled(true);
                if (searchResultTable instanceof com.intellij.ui.table.JBTable jbTable) {
                    jbTable.setPaintBusy(false);
                }
            }

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
                if (isUnavailable()) {
                    return;
                }
                restoreUi();
                if (failure != null) {
                    if (statusLabel != null) {
                        statusLabel.setText("搜索失败");
                    }
                    Messages.showErrorDialog(failure.getMessage(), "错误");
                    return;
                }
                List<Object[]> rows = new ArrayList<>(result.size());
                for (int i = 0; i < result.size(); i++) {
                    LangInfo langInfo = result.get(i);
                    rows.add(new Object[]{i + 1, langInfo.getLineNumber(),
                            langInfo.getLanguage(), langInfo.getText(), langInfo.getPath(),
                            langInfo.getInternalPath()});
                }
                if (searchResultTable.getModel() instanceof BulkTableModel model) {
                    model.replaceRows(rows);
                } else {
                    for (Object[] row : rows) {
                        ((DefaultTableModel) searchResultTable.getModel()).addRow(row);
                    }
                }
                if (statusLabel != null) {
                    statusLabel.setText("找到 " + result.size() + " 条结果");
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
                if (isUnavailable()) {
                    return;
                }
                restoreUi();
                if (statusLabel != null) {
                    statusLabel.setText("已取消搜索");
                }
            }

            @Override
            public void onThrowable(@NotNull Throwable error) {
                if (isUnavailable()) {
                    return;
                }
                restoreUi();
                if (statusLabel != null) {
                    statusLabel.setText("搜索失败");
                }
                String message = StringUtils.defaultIfBlank(
                        error.getMessage(), error.getClass().getName());
                Messages.showErrorDialog("搜索多语文件失败：\n" + message, "错误");
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
