package com.tanner.devconfig;

import com.intellij.openapi.ui.Messages;
import com.tanner.abs.AbstractDialog;
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

    public DataSourceCopyDlg(DevConfigDialog parentDlg) {
        super(parentDlg.getProjectContext());
        this.parentDlg = parentDlg;
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
            Messages.showErrorDialog("请输入数据源名称", "错误");
            return;
        }
        if (parentDlg.getDataSourceMetaMap().containsKey(newName)) {
            Messages.showErrorDialog("数据源名称已存在", "错误");
            return;
        }
        try {
            DataSourceMeta newMeta = (DataSourceMeta) parentDlg.getCurrMeta().clone();
            newMeta.setBase(false);
            newMeta.setDataSourceName(newName);
            parentDlg.developmentDataSourceCheckBox()
                    .setSelected("design".equals(newName));
            parentDlg.baseDataSourceCheckBox().setSelected(false);
            JComboBox<String> box = parentDlg.databaseBox();
            box.addItem(newName);
            box.setSelectedItem(newName);
            parentDlg.getDataSourceMetaMap().put(newName, newMeta);
            parentDlg.setCurrMeta(newMeta);
            DataSourceUtil.saveDesignDataSourceMeta(parentDlg);
        } catch (Exception e) {
            Messages.showErrorDialog(e.getMessage(), "错误");
            return;
        }
        int opt = Messages.showYesNoDialog("Copy success, do you want to exit?", "提示", Messages.getQuestionIcon());
        if (opt == Messages.OK) {
            parentDlg.close(0);
        }
    }

    public DevConfigDialog getParentDlg() {
        return parentDlg;
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return contentPane;
    }

}
