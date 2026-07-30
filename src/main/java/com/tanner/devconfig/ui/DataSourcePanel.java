package com.tanner.devconfig.ui;

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import com.tanner.abs.AbstractDataSourceDialog;

import javax.swing.*;
import java.awt.*;

/**
 * Shared IntelliJ-style datasource editor used by the configuration and export dialogs.
 */
public final class DataSourcePanel {

    private final JPanel panel = new JPanel(new GridBagLayout());
    private final JComboBox<String> dbBox = new JComboBox<>();
    private final JComboBox<String> dbTypeBox = new JComboBox<>();
    private final JComboBox<String> driverBox = new JComboBox<>();
    private final JBTextField hostText = new JBTextField();
    private final JBTextField portText = new JBTextField();
    private final JBTextField dbNameText = new JBTextField();
    private final JBTextField oidText = new JBTextField();
    private final JBTextField userText = new JBTextField();
    private final JBTextField pwdText = new JBTextField();
    private final JCheckBox baseChx = new JCheckBox("基础数据源");
    private final JCheckBox devChx = new JCheckBox("开发数据源");
    private final JPanel dataSourceActions = new JPanel(
            new FlowLayout(FlowLayout.RIGHT, JBUI.scale(6), 0));
    private final JPanel roleActions = new JPanel(
            new FlowLayout(FlowLayout.RIGHT, JBUI.scale(6), 0));

    public DataSourcePanel(AbstractDataSourceDialog dialog, boolean showRoles) {
        panel.setBorder(JBUI.Borders.empty(8));
        int row = 0;
        addWideRow(row++, "数据源", dbBox, dataSourceActions);
        addPairRow(row++, "数据库类型", dbTypeBox, "驱动", driverBox);
        addPairRow(row++, "主机", hostText, "端口", portText);
        addPairRow(row++, "数据库", dbNameText, "OID 标识", oidText);
        addPairRow(row++, "用户名", userText, "密码", pwdText);
        if (showRoles) {
            JPanel roles = new JPanel(new FlowLayout(FlowLayout.LEFT, JBUI.scale(8), 0));
            baseChx.setEnabled(false);
            devChx.setEnabled(false);
            roles.add(baseChx);
            roles.add(devChx);
            addWideRow(row, "用途", roles, roleActions);
        }
        register(dialog);
    }

    private void addWideRow(int row, String label, JComponent component, JComponent actions) {
        GridBagConstraints labelConstraints = constraints(0, row);
        labelConstraints.anchor = GridBagConstraints.WEST;
        panel.add(new JBLabel(label + "："), labelConstraints);

        GridBagConstraints valueConstraints = constraints(1, row);
        valueConstraints.gridwidth = 3;
        valueConstraints.weightx = 1;
        valueConstraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(component, valueConstraints);

        GridBagConstraints actionConstraints = constraints(4, row);
        actionConstraints.anchor = GridBagConstraints.EAST;
        panel.add(actions, actionConstraints);
    }

    private void addPairRow(int row, String leftLabel, JComponent leftComponent,
                            String rightLabel, JComponent rightComponent) {
        GridBagConstraints leftLabelConstraints = constraints(0, row);
        leftLabelConstraints.anchor = GridBagConstraints.WEST;
        panel.add(new JBLabel(leftLabel + "："), leftLabelConstraints);

        GridBagConstraints leftValueConstraints = constraints(1, row);
        leftValueConstraints.weightx = 1;
        leftValueConstraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(leftComponent, leftValueConstraints);

        GridBagConstraints rightLabelConstraints = constraints(2, row);
        rightLabelConstraints.anchor = GridBagConstraints.WEST;
        panel.add(new JBLabel(rightLabel + "："), rightLabelConstraints);

        GridBagConstraints rightValueConstraints = constraints(3, row);
        rightValueConstraints.gridwidth = 2;
        rightValueConstraints.weightx = 1;
        rightValueConstraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(rightComponent, rightValueConstraints);
    }

    private static GridBagConstraints constraints(int column, int row) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = column;
        constraints.gridy = row;
        constraints.insets = JBUI.insets(4);
        return constraints;
    }

    private void register(AbstractDataSourceDialog dialog) {
        dialog.addComponent("dbBox", dbBox);
        dialog.addComponent("dbTypeBox", dbTypeBox);
        dialog.addComponent("driverBox", driverBox);
        dialog.addComponent("hostText", hostText);
        dialog.addComponent("portText", portText);
        dialog.addComponent("dbNameText", dbNameText);
        dialog.addComponent("oidText", oidText);
        dialog.addComponent("userText", userText);
        dialog.addComponent("pwdText", pwdText);
        dialog.addComponent("baseChx", baseChx);
        dialog.addComponent("devChx", devChx);
        dialog.addComponent("dsTab", panel);
    }

    public JPanel getPanel() {
        return panel;
    }

    public JPanel getDataSourceActions() {
        return dataSourceActions;
    }

    public JPanel getRoleActions() {
        return roleActions;
    }

    public JComboBox<String> getDbBox() {
        return dbBox;
    }

    public JComboBox<String> getDbTypeBox() {
        return dbTypeBox;
    }

    public JComboBox<String> getDriverBox() {
        return driverBox;
    }

    public JBTextField getHostText() {
        return hostText;
    }

    public JBTextField getPortText() {
        return portText;
    }

    public JBTextField getDbNameText() {
        return dbNameText;
    }

    public JBTextField getOidText() {
        return oidText;
    }

    public JBTextField getUserText() {
        return userText;
    }

    public JBTextField getPwdText() {
        return pwdText;
    }

    public JCheckBox getBaseChx() {
        return baseChx;
    }

    public JCheckBox getDevChx() {
        return devChx;
    }
}
