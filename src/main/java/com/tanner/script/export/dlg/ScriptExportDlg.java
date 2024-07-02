package com.tanner.script.export.dlg;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.tanner.abs.AbstractDataSourceDialog;
import com.tanner.devconfig.action.button.datasource.TestConnectionAction;
import com.tanner.devconfig.action.item.DBBoxListener;
import com.tanner.devconfig.action.item.DBTypeBoxListener;
import com.tanner.devconfig.action.item.DriverBoxListener;
import com.tanner.devconfig.util.DataSourceUtil;
import com.tanner.script.export.action.ExportAction;
import com.tanner.script.export.action.PathSelAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class ScriptExportDlg extends AbstractDataSourceDialog {

    private JPanel contentPane;
    private JTextField exportPathText;
    private JButton pathSelBtn;
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
    private JTextField heavyNodeCodeText;
    private JTextField lightNodeCodeText;
    private JButton exportBtn;
    private JTextField mdNameText;
    private JTextField mdModuleText;
    private JCheckBox spiltGoCheckBox;
    private JComboBox<?> exportModeComboBox;

    public ScriptExportDlg(AnActionEvent event) {
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
        setSize(580, 560);
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
        addComponent("pathSelBtn", pathSelBtn);
        addComponent("exportPathText", exportPathText);
        addComponent("heavyNodeCodeText", heavyNodeCodeText);
        addComponent("lightNodeCodeText", lightNodeCodeText);
        addComponent("mdNameText", mdNameText);
        addComponent("mdModuleText", mdModuleText);
        addComponent("exportModeComboBox", exportModeComboBox);
        addComponent("spiltGoCheckBox", spiltGoCheckBox);
    }

    private void initListener() {
        //数据源相关按钮
        testBtn.addActionListener(new TestConnectionAction(this));
        //数据源相关下拉
        dbBox.addItemListener(new DBBoxListener(this));
        dbTypeBox.addItemListener(new DBTypeBoxListener(this));
        driverBox.addItemListener(new DriverBoxListener(this));
        pathSelBtn.addActionListener(new PathSelAction(this));
        exportBtn.addActionListener(new ExportAction(this));
    }

    private void initData() {
        File desktopPath = new File(System.getProperty("user.home") + File.separator + "Desktop");
        if (desktopPath.exists()) {
            exportPathText.setText(desktopPath.getAbsolutePath());
        }
        DataSourceUtil.initDataSource(this);
        // 初始化导出模式
        initExportModeBox();
    }

    private void initExportModeBox() {
        String[] exportModes = {"先删除后导入", "仅插入", "仅删除"};
        exportModeComboBox.setModel(new DefaultComboBoxModel(exportModes));
        exportModeComboBox.getModel().setSelectedItem(exportModes[0]);
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
