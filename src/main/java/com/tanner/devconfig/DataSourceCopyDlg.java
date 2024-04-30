package com.tanner.devconfig;

import com.intellij.openapi.ui.Messages;
import com.tanner.abs.AbstractDialog;
import com.tanner.base.ProjectManager;
import com.tanner.devconfig.util.DataSourceUtil;
import com.tanner.prop.entity.DataSourceMeta;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * 复制数据源
 */
public class DataSourceCopyDlg extends AbstractDialog {

    private JPanel contentPane;
    private JTextField newNameText;

    private DevConfigDialog parentDlg;

    public DataSourceCopyDlg() {
        super(ProjectManager.getInstance().getProject());
        init();
        //获取显示屏尺寸，使界面居中
        int width = Toolkit.getDefaultToolkit().getScreenSize().width;
        int height = Toolkit.getDefaultToolkit().getScreenSize().height;
        setLocation((width - 600) / 2, (height - 200) / 2);
        setSize(600, 200);
    }

    @Override
    protected void doOKAction() {
        String newName = newNameText.getText();
        if (StringUtils.isBlank(newName)) {
            Messages.showErrorDialog("DataSource name can not be null!", "出错了");
            return;
        }
        if (parentDlg.getDataSourceMetaMap().containsKey(newName)) {
            Messages.showErrorDialog("DataSource is existed!", "出错了");
            return;
        }
        try {
            DataSourceMeta newMeta = (DataSourceMeta) parentDlg.getCurrMeta().clone();
            newMeta.setBase(false);
            newMeta.setDataSourceName(newName);
            parentDlg.getComponent(JCheckBox.class, "devChx").setSelected("design".equals(newName));
            parentDlg.getComponent(JCheckBox.class, "baseChx").setSelected(false);
            JComboBox box = parentDlg.getComponent(JComboBox.class, "dbBox");
            box.addItem(newName);
            box.setSelectedItem(newName);
            parentDlg.getDataSourceMetaMap().put(newName, newMeta);
            parentDlg.setCurrMeta(newMeta);
        } catch (Exception e) {
            Messages.showErrorDialog(e.getMessage(), "出错了");
            return;
        }
        int opt = Messages.showYesNoDialog("Copy success, do you want to exit?", "提示", Messages.getQuestionIcon());
        if (opt == Messages.OK) {
            DataSourceUtil.saveDesignDataSourceMeta(parentDlg);
            parentDlg.close(0);
        }
    }

    public DevConfigDialog getParentDlg() {
        return parentDlg;
    }

    public void setParentDlg(DevConfigDialog parentDlg) {
        this.parentDlg = parentDlg;
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return contentPane;
    }

}
