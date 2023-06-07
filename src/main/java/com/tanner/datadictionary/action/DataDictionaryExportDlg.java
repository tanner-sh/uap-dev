package com.tanner.datadictionary.action;

import com.tanner.abs.AbstractDataSourceDialog;
import com.tanner.devconfig.action.button.datasource.TestConnectionAction;
import com.tanner.devconfig.action.item.DBBoxListener;
import com.tanner.devconfig.action.item.DBTypeBoxListener;
import com.tanner.devconfig.action.item.DriverBoxListener;
import com.tanner.devconfig.util.DataSourceUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class DataDictionaryExportDlg extends AbstractDataSourceDialog {

    private JPanel contentPane;
    private JPanel dsTab;
    private JComboBox<?> dbBox;
    private JButton testBtn;
    private JComboBox<?> dbTypeBox;
    private JComboBox<?> driverBox;
    private JTextField hostText;
    private JTextField portText;
    private JTextField dbNameText;
    private JTextField oidText;
    private JTextField userText;
    private JTextField pwdText;
    private JButton loadBtn;
    private JButton exportBtn;
    private JTable dbTable;
    private JButton selectAllBtn;
    private JButton deSelectAllBtn;
    private JTextField filterTextField;
    private JComboBox<?> exportAsBox;

    public DataDictionaryExportDlg() {
        initUI();
        initListener();
        initData();
    }

    private void initUI() {
        setContentPane(contentPane);
        setModal(true);
        //获取显示屏尺寸，使界面居中
        int width = Toolkit.getDefaultToolkit().getScreenSize().width;
        int height = Toolkit.getDefaultToolkit().getScreenSize().height;
        this.setBounds((width - 800) / 2, (height - 600) / 2, 580, 560);
        //JComponent 集合
        addComponent("dbBox", dbBox);
        addComponent("dbTypeBox", dbTypeBox);
        addComponent("driverBox", driverBox);
        addComponent("hostText", hostText);
        addComponent("portText", portText);
        addComponent("dbNameText", dbNameText);
        addComponent("oidText", oidText);
        addComponent("userText", userText);
        addComponent("pwdText", pwdText);
        addComponent("dsTab", dsTab);
        addComponent("testBtn", testBtn);
        addComponent("loadBtn", loadBtn);
        addComponent("exportBtn", exportBtn);
        addComponent("dbTable", dbTable);
        addComponent("selectAllBtn", selectAllBtn);
        addComponent("deSelectAllBtn", deSelectAllBtn);
        addComponent("filterTextField", filterTextField);
        addComponent("exportAsBox", exportAsBox);
    }

    private void initListener() {
        //数据源相关按钮
        testBtn.addActionListener(new TestConnectionAction(this));
        //数据源相关下拉
        dbBox.addItemListener(new DBBoxListener(this));
        dbTypeBox.addItemListener(new DBTypeBoxListener(this));
        driverBox.addItemListener(new DriverBoxListener(this));
        loadBtn.addActionListener(new LoadAction(this));
        exportBtn.addActionListener(new ExportAction(this));
        selectAllBtn.addActionListener(new SelectAllAction(this));
        deSelectAllBtn.addActionListener(new DeSelectAllAction(this));
    }

    private void initData() {
        //初始化数据源
        DataSourceUtil.initDataSource(this);
        //初始化表格
        initDbTable();
        //初始化导出设置
        initExportBox();
    }

    private void initExportBox() {
        String[] exports = {"pdf", "markdown", "html"};
        exportAsBox.setModel(new DefaultComboBoxModel(exports));
        exportAsBox.getModel().setSelectedItem(exports[0]);
    }

    private void initDbTable() {
        DefaultTableModel tableModel = new DefaultTableModel(null,
                new String[]{"序号", "选中", "表名", "表备注"}) {

            @Override
            public Class<?> getColumnClass(int c) {
                return switch (c) {
                    case 0 -> Integer.class;
                    case 1 -> Boolean.class;
                    default -> String.class;
                };
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1;
            }

        };
        dbTable.setModel(tableModel);
    }

}
