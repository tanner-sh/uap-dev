package com.tanner.devconfig.action.listenner;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.ui.Messages;
import com.tanner.abs.AbstractDialog;
import com.tanner.abs.AbstractTabListener;
import com.tanner.base.UapProjectEnvironment;
import com.tanner.devconfig.DevConfigDialog;
import com.tanner.devconfig.util.TableModelUtil;
import com.tanner.ui.BulkTableModel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.event.MouseEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 设置页面切换监听
 */
public class ConfigTabbedChangeListener extends AbstractTabListener {

    private final AtomicBoolean loading = new AtomicBoolean();
    private final AtomicBoolean reloadRequested = new AtomicBoolean();
    private final AtomicLong loadVersion = new AtomicLong();
    private volatile boolean initialized;

    public ConfigTabbedChangeListener(DevConfigDialog dlg) {
        super(dlg);
    }

    @Override
    protected void afterChange(ChangeEvent event, AbstractDialog dlg) {
        DevConfigDialog view = (DevConfigDialog) dlg;
        JTabbedPane tabbedPane = view.tabs();
        int index = tabbedPane.getSelectedIndex();
        if (index == 1) {
            loadModules(false);
        }
    }

    public void reloadModules() {
        loadModules(true);
    }

    public void invalidate() {
        initialized = false;
        loadVersion.incrementAndGet();
    }

    public boolean isInitialized() {
        return initialized;
    }

    private void loadModules(boolean force) {
        if (!force && initialized) {
            return;
        }
        if (!loading.compareAndSet(false, true)) {
            reloadRequested.set(true);
            loadVersion.incrementAndGet();
            return;
        }
        long version = loadVersion.incrementAndGet();
        DevConfigDialog dialog = (DevConfigDialog) getDlg();
        JTable mustTable = dialog.requiredModulesTable();
        JTable selectedTable = dialog.selectedModulesTable();
        setBusy(mustTable, true);
        setBusy(selectedTable, true);
        UapProjectEnvironment environment = UapProjectEnvironment.getInstance(
                dialog.getProjectContext());
        if (environment == null) {
            loading.set(false);
            setBusy(mustTable, false);
            setBusy(selectedTable, false);
            return;
        }
        String homePath = dialog.homeField().getText();
        if (homePath == null || homePath.isBlank()) {
            homePath = environment.getUapHomePath();
        }
        if (homePath == null || homePath.isBlank()) {
            loading.set(false);
            setBusy(mustTable, false);
            setBusy(selectedTable, false);
            return;
        }
        String mustModules = environment.getMust_modules();
        String excludedModules = environment.getEx_modules();
        String finalHomePath = homePath;
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            TableModelUtil.ModuleTableData result = null;
            Exception failure = null;
            try {
                result = TableModelUtil.loadModuleData(finalHomePath, mustModules, excludedModules);
            } catch (Exception exception) {
                failure = exception;
            }
            TableModelUtil.ModuleTableData finalResult = result;
            Exception finalFailure = failure;
            ApplicationManager.getApplication().invokeLater(() -> {
                try {
                    if (dialog.isDialogDisposed()) {
                        return;
                    }
                    if (loadVersion.get() != version) {
                        return;
                    }
                    if (finalFailure != null) {
                        Messages.showErrorDialog(dialog.getProjectContext(),
                                "加载模块失败：\n" + finalFailure.getMessage(), "错误");
                        return;
                    }
                    if (mustTable.getModel() instanceof BulkTableModel mustModel
                            && selectedTable.getModel() instanceof BulkTableModel selectedModel) {
                        mustModel.replaceRows(finalResult.mustRows());
                        selectedModel.replaceRows(finalResult.selectedRows());
                        initialized = true;
                    }
                } finally {
                    loading.set(false);
                    setBusy(mustTable, false);
                    setBusy(selectedTable, false);
                    if (!dialog.isDialogDisposed()
                            && reloadRequested.getAndSet(false)) {
                        loadModules(true);
                    }
                }
            }, ModalityState.any());
        });
    }

    private static void setBusy(JTable table, boolean busy) {
        if (table instanceof com.intellij.ui.table.JBTable jbTable) {
            jbTable.setPaintBusy(busy);
        }
    }

    @Override
    protected void click(MouseEvent event, AbstractDialog dlg) {
    }

}
