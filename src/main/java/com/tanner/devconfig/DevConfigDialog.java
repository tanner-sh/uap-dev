package com.tanner.devconfig;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.table.JBTable;
import com.intellij.ui.TitledSeparator;
import com.intellij.util.ui.JBUI;
import com.tanner.abs.AbstractDataSourceDialog;
import com.tanner.base.UapProjectEnvironment;
import com.tanner.devconfig.action.button.ApplyAction;
import com.tanner.devconfig.action.button.OKAction;
import com.tanner.devconfig.action.button.SelHomePathAction;
import com.tanner.devconfig.action.button.SetLibraryAction;
import com.tanner.devconfig.action.button.datasource.CopyDataSourceAction;
import com.tanner.devconfig.action.button.datasource.DeleteDataSourceAction;
import com.tanner.devconfig.action.button.datasource.SetBaseDataSourceAction;
import com.tanner.devconfig.action.button.datasource.SetDevDataSourceAction;
import com.tanner.devconfig.action.button.datasource.TestConnectionAction;
import com.tanner.devconfig.action.button.module.CancelAllAction;
import com.tanner.devconfig.action.button.module.DefaultModuleAction;
import com.tanner.devconfig.action.button.module.SelAllAction;
import com.tanner.devconfig.action.item.DBBoxListener;
import com.tanner.devconfig.action.item.DBTypeBoxListener;
import com.tanner.devconfig.action.item.DriverBoxListener;
import com.tanner.devconfig.action.listenner.ConfigTabbedChangeListener;
import com.tanner.devconfig.ui.DataSourcePanel;
import com.tanner.devconfig.util.DataSourceUtil;
import com.tanner.devconfig.util.TableModelUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * IntelliJ-style development configuration dialog.
 */
public class DevConfigDialog extends AbstractDataSourceDialog {

    private final JPanel contentPane = new JPanel(new BorderLayout(0, JBUI.scale(8)));
    private final JTabbedPane tabbedPane = new JTabbedPane();
    private final JBTextField homeText = new JBTextField();
    private final JButton homeSelBtn = new JButton("选择…");
    private final JButton setLibBtn = new JButton("设置类路径");
    private final JButton testBtn = new JButton("测试连接");
    private final JButton setDevBtn = new JButton("设为开发");
    private final JButton setBaseBtn = new JButton("设为基础");
    private final JButton copyBtn = new JButton("复制");
    private final JButton delBtn = new JButton("删除");
    private final JButton defaultBtn = new JButton("恢复默认");
    private final JButton selAllLBtn = new JButton("全选");
    private final JButton cancelAllLBtn = new JButton("取消全选");
    private final JButton mustBtn = new JButton("同步必选");
    private final JButton selAllRBtn = new JButton("全选");
    private final JButton cancelRBtn = new JButton("取消全选");
    private final JButton refreshModulesBtn = new JButton("刷新");
    private final JBTable mustTable = new JBTable();
    private final JBTable selTable = new JBTable();
    private final DataSourcePanel dataSourcePanel;
    private final ConfigTabbedChangeListener moduleListener;
    private Action applyDialogAction;

    private boolean libFlag;

    public DevConfigDialog(AnActionEvent event) {
        super(event.getProject());
        dataSourcePanel = new DataSourcePanel(this, true);
        moduleListener = new ConfigTabbedChangeListener(this);
        buildUi();
        registerComponents();
        setTitle("集成配置");
        setResizable(true);
        init();
        initListeners();
        initPath();
    }

    private void buildUi() {
        contentPane.setBorder(JBUI.Borders.empty(8));
        contentPane.setPreferredSize(JBUI.size(860, 580));

        JPanel homePanel = new JPanel(new BorderLayout(0, JBUI.scale(6)));
        JPanel homeRow = new JPanel(new GridBagLayout());
        GridBagConstraints label = new GridBagConstraints();
        label.gridx = 0;
        label.insets = JBUI.insets(6);
        label.anchor = GridBagConstraints.WEST;
        homeRow.add(new JBLabel("目录："), label);
        GridBagConstraints value = new GridBagConstraints();
        value.gridx = 1;
        value.weightx = 1;
        value.fill = GridBagConstraints.HORIZONTAL;
        value.insets = JBUI.insets(6);
        homeRow.add(homeText, value);
        GridBagConstraints choose = new GridBagConstraints();
        choose.gridx = 2;
        choose.insets = JBUI.insets(6, 0, 6, 6);
        homeRow.add(homeSelBtn, choose);
        GridBagConstraints libraries = new GridBagConstraints();
        libraries.gridx = 3;
        libraries.insets = JBUI.insets(6, 0, 6, 6);
        homeRow.add(setLibBtn, libraries);
        homePanel.add(new TitledSeparator("NC Home"), BorderLayout.NORTH);
        homePanel.add(homeRow, BorderLayout.CENTER);

        JPanel dataSourceActions = dataSourcePanel.getDataSourceActions();
        dataSourceActions.add(testBtn);
        dataSourceActions.add(copyBtn);
        dataSourceActions.add(delBtn);
        JPanel roleActions = dataSourcePanel.getRoleActions();
        roleActions.add(setDevBtn);
        roleActions.add(setBaseBtn);
        JPanel datasourceTab = new JPanel(new BorderLayout());
        datasourceTab.add(dataSourcePanel.getPanel(), BorderLayout.NORTH);

        mustTable.setModel(TableModelUtil.getMustModel(this));
        selTable.setModel(TableModelUtil.getSelModel(this));
        configureModuleTable(mustTable, "尚未加载模块");
        configureModuleTable(selTable, "尚未加载模块");
        JPanel mustPanel = createModulePanel("必选模块", mustTable,
                defaultBtn, selAllLBtn, cancelAllLBtn);
        JPanel selectedPanel = createModulePanel("启动模块", selTable,
                mustBtn, selAllRBtn, cancelRBtn, refreshModulesBtn);
        JPanel modulePanel = new JPanel(new GridLayout(1, 2, JBUI.scale(8), 0));
        modulePanel.setBorder(JBUI.Borders.emptyTop(8));
        modulePanel.add(mustPanel);
        modulePanel.add(selectedPanel);

        tabbedPane.addTab("数据源", datasourceTab);
        tabbedPane.addTab("模块", modulePanel);
        contentPane.add(homePanel, BorderLayout.NORTH);
        contentPane.add(tabbedPane, BorderLayout.CENTER);
    }

    private static JPanel createModulePanel(String title, JBTable table, JButton... buttons) {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, JBUI.scale(6), 0));
        for (JButton button : buttons) {
            toolbar.add(button);
        }
        panel.add(new TitledSeparator(title, toolbar), BorderLayout.NORTH);
        panel.add(new com.intellij.ui.components.JBScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private static void configureModuleTable(JBTable table, String emptyText) {
        table.setStriped(true);
        table.setShowVerticalLines(false);
        table.setAutoCreateRowSorter(true);
        table.getEmptyText().setText(emptyText);
        table.setFillsViewportHeight(true);
    }

    private void registerComponents() {
        addComponent("homeText", homeText);
        addComponent("tabbedPane", tabbedPane);
        addComponent("moduleTab", tabbedPane);
        addComponent("homeSelBtn", homeSelBtn);
        addComponent("testBtn", testBtn);
        addComponent("setDevBtn", setDevBtn);
        addComponent("setBaseBtn", setBaseBtn);
        addComponent("copyBtn", copyBtn);
        addComponent("delBtn", delBtn);
        addComponent("mustTable", mustTable);
        addComponent("selTable", selTable);
        addComponent("setLibBtn", setLibBtn);
    }

    private void initPath() {
        UapProjectEnvironment environment = UapProjectEnvironment.getInstance(getProjectContext());
        if (environment != null) {
            homeText.setText(environment.getUapHomePath());
        }
        DataSourceUtil.initDataSourceAsync(this);
    }

    private void initListeners() {
        tabbedPane.addChangeListener(moduleListener);
        homeSelBtn.addActionListener(new SelHomePathAction(this));
        setLibBtn.addActionListener(new SetLibraryAction(this));
        testBtn.addActionListener(new TestConnectionAction(this));
        setDevBtn.addActionListener(new SetDevDataSourceAction(this));
        setBaseBtn.addActionListener(new SetBaseDataSourceAction(this));
        copyBtn.addActionListener(new CopyDataSourceAction(this));
        delBtn.addActionListener(new DeleteDataSourceAction(this));
        dataSourcePanel.getDbBox().addItemListener(new DBBoxListener(this));
        dataSourcePanel.getDbTypeBox().addItemListener(new DBTypeBoxListener(this));
        dataSourcePanel.getDriverBox().addItemListener(new DriverBoxListener(this));
        defaultBtn.addActionListener(new DefaultModuleAction(this, TableModelUtil.MODULE_TYPE_MUST));
        selAllLBtn.addActionListener(new SelAllAction(this, TableModelUtil.MODULE_TYPE_MUST));
        cancelAllLBtn.addActionListener(new CancelAllAction(this, TableModelUtil.MODULE_TYPE_MUST));
        mustBtn.addActionListener(new DefaultModuleAction(this, TableModelUtil.MODULE_TYPE_SEL));
        selAllRBtn.addActionListener(new SelAllAction(this, TableModelUtil.MODULE_TYPE_SEL));
        cancelRBtn.addActionListener(new CancelAllAction(this, TableModelUtil.MODULE_TYPE_SEL));
        refreshModulesBtn.addActionListener(event -> moduleListener.reloadModules());
    }

    @Override
    protected void doOKAction() {
        new OKAction(this).actionPerformed(
                new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "ok"));
    }

    @Override
    protected Action @NotNull [] createActions() {
        getOKAction().putValue(Action.NAME, "确定");
        getCancelAction().putValue(Action.NAME, "取消");
        applyDialogAction = new AbstractAction("应用") {
            @Override
            public void actionPerformed(ActionEvent event) {
                new ApplyAction(DevConfigDialog.this).actionPerformed(event);
            }
        };
        return new Action[]{getOKAction(), applyDialogAction, getCancelAction()};
    }

    @Override
    protected void onDataSourceLoadingChanged(boolean loading) {
        setOKActionEnabled(!loading);
        if (applyDialogAction != null) {
            applyDialogAction.setEnabled(!loading);
        }
    }

    @Override
    protected String getDimensionServiceKey() {
        return "uap.dev.config.dialog.v2";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return homeText;
    }

    public int getTabIndex() {
        return tabbedPane.getSelectedIndex();
    }

    public JTextField homeField() {
        return homeText;
    }

    public JTable requiredModulesTable() {
        return mustTable;
    }

    public JTable selectedModulesTable() {
        return selTable;
    }

    public JTabbedPane tabs() {
        return tabbedPane;
    }

    public boolean isLibFlag() {
        return libFlag;
    }

    public void setLibFlag(boolean libFlag) {
        this.libFlag = libFlag;
    }

    public void invalidateModules() {
        moduleListener.invalidate();
        if (tabbedPane.getSelectedIndex() == 1) {
            moduleListener.reloadModules();
        }
    }

    public boolean isModulesInitialized() {
        return moduleListener.isInitialized();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return contentPane;
    }
}
