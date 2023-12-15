package com.tanner.langsearch;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.tanner.abs.AbstractDataSourceDialog;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

public class LangSearchDlg extends AbstractDataSourceDialog {

    private JPanel contentPane;
    private JTextField searchTextField;
    private JButton searchBtn;
    private JTable searchResultTable;

    public LangSearchDlg(AnActionEvent event) {
        super(event.getProject());
        init();
        initUI();
        initListener();
        initData();
    }

    private void initUI() {
        //获取显示屏尺寸，使界面居中
        int width = Toolkit.getDefaultToolkit().getScreenSize().width;
        int height = Toolkit.getDefaultToolkit().getScreenSize().height;
        setLocation((width - 800) / 2, (height - 600) / 2);
        setSize(1200, 480);
        //JComponent 集合
        addComponent("searchTextField", searchTextField);
        addComponent("searchBtn", searchBtn);
        addComponent("searchResultTable", searchResultTable);
        searchResultTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = searchResultTable.getSelectedRow();
                    if (row >= 0) {
                        String filePath = (String) searchResultTable.getValueAt(row, 4);
                        File file = new File(filePath);
                        if (!file.exists()) {
                            Messages.showInfoMessage("文件不存在: " + filePath, "提示");
                            return;
                        }
                        String osName = System.getProperty("os.name");
                        try {
                            if (osName.startsWith("Windows")) {
                                Runtime.getRuntime().exec("explorer /select, " + filePath);
                            } else if (osName.startsWith("Mac")) {
                                Runtime.getRuntime().exec("open -R " + filePath);
                            }
                        } catch (IOException ex) {
                            Messages.showInfoMessage("打开文件出错!: " + filePath, "提示");
                        }
                    }
                }
            }
        });
    }

    private void initListener() {
        //数据源相关按钮
        searchBtn.addActionListener(new SearchAction(this));
        searchTextField.addActionListener(new SearchAction(this));
    }

    private void initData() {
        //初始化表格
        initTable();
    }

    private void initTable() {
        // 设置列名
        DefaultTableModel tableModel = new DefaultTableModel(null,
                new String[]{"序号", "行号", "语言", "内容", "文件位置", "内部路径"}) {

            @Override
            public Class<?> getColumnClass(int c) {
                if (c == 0 || c == 1) {
                    return Integer.class;
                } else {
                    return String.class;
                }
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

        };
        searchResultTable.setModel(tableModel);
        // 设置每列的宽度
        TableColumnModel columnModel = searchResultTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(40);
        columnModel.getColumn(1).setPreferredWidth(40);
        columnModel.getColumn(2).setPreferredWidth(60);
        columnModel.getColumn(3).setPreferredWidth(300);
        columnModel.getColumn(4).setPreferredWidth(480);
        columnModel.getColumn(5).setPreferredWidth(280);
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
