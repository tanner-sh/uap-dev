package com.tanner.langsearch;

import com.intellij.ide.actions.RevealFileAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.SearchTextField;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import com.tanner.abs.AbstractDataSourceDialog;
import com.tanner.ui.BulkTableModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Set;

public class LangSearchDlg extends AbstractDataSourceDialog {

    private final JPanel contentPane = new JPanel(new BorderLayout(0, JBUI.scale(8)));
    private final SearchTextField searchField = new SearchTextField(true);
    private final JButton searchBtn = new JButton("搜索");
    private final JBLabel statusLabel = new JBLabel("输入关键字后开始搜索");
    private final JBTable searchResultTable = new JBTable(new BulkTableModel(
            new String[]{"序号", "行号", "语言", "内容", "文件位置", "内部路径"},
            new Class<?>[]{Integer.class, Integer.class, String.class, String.class,
                    String.class, String.class},
            Set.of()));

    public LangSearchDlg(AnActionEvent event) {
        super(event.getProject());
        buildUi();
        registerComponents();
        setTitle("多语搜索");
        setResizable(true);
        init();
        initListeners();
    }

    private void buildUi() {
        contentPane.setBorder(JBUI.Borders.empty(12));
        contentPane.setPreferredSize(JBUI.size(1000, 520));
        searchField.getTextEditor().getEmptyText().setText("搜索语言包中的键或文本");

        JPanel searchBar = new JPanel(new BorderLayout(JBUI.scale(8), 0));
        searchBar.add(searchField, BorderLayout.CENTER);
        searchBar.add(searchBtn, BorderLayout.EAST);

        searchResultTable.setStriped(true);
        searchResultTable.setShowVerticalLines(false);
        searchResultTable.setFillsViewportHeight(true);
        searchResultTable.setAutoCreateRowSorter(true);
        searchResultTable.getEmptyText().setText("暂无搜索结果");
        searchResultTable.getColumnModel().getColumn(0).setPreferredWidth(JBUI.scale(50));
        searchResultTable.getColumnModel().getColumn(1).setPreferredWidth(JBUI.scale(50));
        searchResultTable.getColumnModel().getColumn(2).setPreferredWidth(JBUI.scale(80));
        searchResultTable.getColumnModel().getColumn(3).setPreferredWidth(JBUI.scale(320));
        searchResultTable.getColumnModel().getColumn(4).setPreferredWidth(JBUI.scale(360));
        searchResultTable.getColumnModel().getColumn(5).setPreferredWidth(JBUI.scale(240));

        contentPane.add(searchBar, BorderLayout.NORTH);
        contentPane.add(new JBScrollPane(searchResultTable), BorderLayout.CENTER);
        contentPane.add(statusLabel, BorderLayout.SOUTH);
    }

    private void registerComponents() {
        addComponent("searchTextField", searchField.getTextEditor());
        addComponent("searchBtn", searchBtn);
        addComponent("searchResultTable", searchResultTable);
        addComponent("statusLabel", statusLabel);
    }

    private void initListeners() {
        SearchAction searchAction = new SearchAction(this);
        searchBtn.addActionListener(searchAction);
        searchField.getTextEditor().addActionListener(searchAction);
        searchResultTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() != 2) {
                    return;
                }
                int viewRow = searchResultTable.getSelectedRow();
                if (viewRow < 0) {
                    return;
                }
                int modelRow = searchResultTable.convertRowIndexToModel(viewRow);
                String filePath = String.valueOf(
                        searchResultTable.getModel().getValueAt(modelRow, 4));
                File file = new File(filePath);
                if (!file.exists()) {
                    Messages.showInfoMessage("文件不存在：" + filePath, "提示");
                    return;
                }
                RevealFileAction.openFile(file);
            }
        });
    }

    @Override
    protected String getDimensionServiceKey() {
        return "uap.language.search.dialog.v2";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return searchField;
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return contentPane;
    }

    @Override
    protected Action @NotNull [] createActions() {
        return new Action[0];
    }
}
