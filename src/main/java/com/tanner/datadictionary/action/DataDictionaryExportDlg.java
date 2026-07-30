package com.tanner.datadictionary.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.SearchTextField;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.ui.TitledSeparator;
import com.intellij.util.ui.JBUI;
import com.tanner.abs.AbstractDataSourceDialog;
import com.tanner.devconfig.action.button.datasource.TestConnectionAction;
import com.tanner.devconfig.action.item.DBBoxListener;
import com.tanner.devconfig.action.item.DBTypeBoxListener;
import com.tanner.devconfig.action.item.DriverBoxListener;
import com.tanner.devconfig.ui.DataSourcePanel;
import com.tanner.devconfig.util.DataSourceUtil;
import com.tanner.ui.BulkTableModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

public class DataDictionaryExportDlg extends AbstractDataSourceDialog {

    private final JPanel contentPane = new JPanel(new BorderLayout(0, JBUI.scale(8)));
    private final DataSourcePanel dataSourcePanel;
    private final JButton testBtn = new JButton("测试连接");
    private final JButton loadBtn = new JButton("加载表");
    private final JButton exportBtn = new JButton("导出");
    private final JButton selectAllBtn = new JButton("全选");
    private final JButton deSelectAllBtn = new JButton("取消全选");
    private final SearchTextField filterField = new SearchTextField(false);
    private final JComboBox<String> exportAsBox = new JComboBox<>(
            new String[]{"pdf", "markdown", "html"});
    private final JCheckBox needFilterDefField = new JCheckBox("过滤自定义项目字段");
    private final JBTable dbTable = new JBTable(new BulkTableModel(
            new String[]{"序号", "选中", "表名", "表备注"},
            new Class<?>[]{Integer.class, Boolean.class, String.class, String.class},
            Set.of(1)));
    private final JPanel logPanel = new JPanel(new BorderLayout(JBUI.scale(8), 0));
    private final JProgressBar progressBar = new JProgressBar();
    private final JBLabel statusLabel = new JBLabel("尚未加载数据表");

    public DataDictionaryExportDlg(AnActionEvent event) {
        super(event.getProject());
        dataSourcePanel = new DataSourcePanel(this, false);
        buildUi();
        registerComponents();
        setTitle("导出数据字典");
        setResizable(true);
        init();
        initListeners();
        DataSourceUtil.initDataSourceAsync(this);
    }

    private void buildUi() {
        contentPane.setBorder(JBUI.Borders.empty(12));
        contentPane.setPreferredSize(JBUI.size(820, 620));
        dataSourcePanel.getDataSourceActions().add(testBtn);

        filterField.getTextEditor().getEmptyText().setText("表名过滤，多个条件使用分号分隔");
        JPanel queryBar = new JPanel(new BorderLayout(JBUI.scale(8), 0));
        queryBar.add(filterField, BorderLayout.CENTER);
        JPanel queryButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, JBUI.scale(6), 0));
        queryButtons.add(loadBtn);
        queryButtons.add(selectAllBtn);
        queryButtons.add(deSelectAllBtn);
        queryBar.add(queryButtons, BorderLayout.EAST);
        queryBar.setBorder(JBUI.Borders.empty(4, 8));

        dbTable.setStriped(true);
        dbTable.setShowVerticalLines(false);
        dbTable.setFillsViewportHeight(true);
        dbTable.setAutoCreateRowSorter(true);
        dbTable.getEmptyText().setText("加载后将在此显示数据表");
        dbTable.getColumnModel().getColumn(0).setPreferredWidth(JBUI.scale(55));
        dbTable.getColumnModel().getColumn(1).setPreferredWidth(JBUI.scale(55));
        dbTable.getColumnModel().getColumn(2).setPreferredWidth(JBUI.scale(260));
        dbTable.getColumnModel().getColumn(3).setPreferredWidth(JBUI.scale(320));

        JPanel center = new JPanel(new BorderLayout(0, JBUI.scale(8)));
        center.add(createSection("表过滤", queryBar), BorderLayout.NORTH);
        center.add(new JBScrollPane(dbTable), BorderLayout.CENTER);

        JPanel exportOptions = new JPanel(new FlowLayout(FlowLayout.RIGHT, JBUI.scale(8), 0));
        exportOptions.add(new JBLabel("格式："));
        exportOptions.add(exportAsBox);
        exportOptions.add(needFilterDefField);
        exportOptions.add(exportBtn);
        logPanel.add(statusLabel, BorderLayout.WEST);
        logPanel.add(progressBar, BorderLayout.CENTER);
        logPanel.add(exportOptions, BorderLayout.EAST);
        progressBar.setPreferredSize(JBUI.size(160, 18));

        contentPane.add(createSection("数据库连接", dataSourcePanel.getPanel()),
                BorderLayout.NORTH);
        contentPane.add(center, BorderLayout.CENTER);
        contentPane.add(logPanel, BorderLayout.SOUTH);
    }

    private static JPanel createSection(String title, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(0, JBUI.scale(4)));
        panel.add(new TitledSeparator(title), BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    private void registerComponents() {
        addComponent("testBtn", testBtn);
        addComponent("loadBtn", loadBtn);
        addComponent("exportBtn", exportBtn);
        addComponent("dbTable", dbTable);
        addComponent("selectAllBtn", selectAllBtn);
        addComponent("deSelectAllBtn", deSelectAllBtn);
        addComponent("filterTextField", filterField.getTextEditor());
        addComponent("exportAsBox", exportAsBox);
        addComponent("needFilterDefField", needFilterDefField);
        addComponent("logPanel", logPanel);
        addComponent("progressBar", progressBar);
        addComponent("statusLabel", statusLabel);
    }

    private void initListeners() {
        testBtn.addActionListener(new TestConnectionAction(this));
        dataSourcePanel.getDbBox().addItemListener(new DBBoxListener(this));
        dataSourcePanel.getDbTypeBox().addItemListener(new DBTypeBoxListener(this));
        dataSourcePanel.getDriverBox().addItemListener(new DriverBoxListener(this));
        loadBtn.addActionListener(new LoadAction(this));
        exportBtn.addActionListener(new ExportAction(this));
        selectAllBtn.addActionListener(new SelectAllAction(this));
        deSelectAllBtn.addActionListener(new DeSelectAllAction(this));
        filterField.getTextEditor().addActionListener(new LoadAction(this));
    }

    @Override
    protected String getDimensionServiceKey() {
        return "uap.data.dictionary.dialog.v2";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return filterField;
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return contentPane;
    }

    @Override
    protected Action @NotNull [] createActions() {
        return new Action[0];
    }

    public JButton loadButton() {
        return loadBtn;
    }

    public JButton exportButton() {
        return exportBtn;
    }

    public JTable table() {
        return dbTable;
    }

    public JTextField filterField() {
        return filterField.getTextEditor();
    }

    public JComboBox<String> exportFormatBox() {
        return exportAsBox;
    }

    public JCheckBox filterDefaultFieldsCheckBox() {
        return needFilterDefField;
    }

    public JProgressBar progressBar() {
        return progressBar;
    }

    public JLabel statusLabel() {
        return statusLabel;
    }
}
