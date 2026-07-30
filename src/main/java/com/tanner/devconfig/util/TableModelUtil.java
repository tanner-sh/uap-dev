package com.tanner.devconfig.util;

import com.tanner.abs.AbstractDialog;
import com.tanner.base.BusinessException;
import com.tanner.base.ModuleFileUtil;
import com.tanner.base.UapProjectEnvironment;
import com.tanner.base.XmlUtil;
import com.tanner.debug.util.CreatApplicationConfigurationUtil;
import com.tanner.devconfig.DevConfigDialog;
import com.tanner.ui.BulkTableModel;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Objects;

public class TableModelUtil {

    /**
     * 必选模块面板
     **/
    public final static int MODULE_TYPE_MUST = 0;

    /**
     * 启动模块面板
     **/
    public final static int MODULE_TYPE_SEL = 1;

    public static BulkTableModel getMustModel(AbstractDialog dialog) {
        return new BulkTableModel(
                new String[]{"序号", "选中", "模块名称"},
                new Class<?>[]{Integer.class, Boolean.class, String.class},
                Set.of(1)) {
            @Override
            public boolean isCellEditable(int row, int column) {
                Object moduleName = getValueAt(row, 2);
                return column == 1 && !ModuleFileUtil.getMustMoudleSet().contains(moduleName);
            }
        };
    }

    public static BulkTableModel getSelModel(AbstractDialog dialog) {
        return new BulkTableModel(
                new String[]{"序号", "启动", "模块名称"},
                new Class<?>[]{Integer.class, Boolean.class, String.class},
                Set.of(1));
    }

    public static void modelHandle(AbstractDialog dialog, BulkTableModel mustModel,
                                   BulkTableModel selModel) {
        UapProjectEnvironment environment = UapProjectEnvironment.getInstance(
                dialog.getProjectContext());
        String homePath = environment.getUapHomePath();
        if (StringUtils.isBlank(homePath) && dialog instanceof DevConfigDialog view) {
            homePath = view.homeField().getText();
        }
        if (StringUtils.isBlank(homePath)) {
            return;
        }
        ModuleTableData data = loadModuleData(homePath, environment.getMust_modules(),
                environment.getEx_modules());
        mustModel.replaceRows(data.mustRows());
        selModel.replaceRows(data.selectedRows());
    }

    public static ModuleTableData loadModuleData(String homePath, String mustModuleStr,
                                                 String selModuleStr) {
        File moduleFile = new File(homePath + File.separator + "modules");
        List<String> moduleList = new ArrayList<>();
        if (moduleFile.exists()) {
            File[] moduleArr = moduleFile.listFiles();
            if (moduleArr != null) {
                for (File module : moduleArr) {
                    String moduleName = getNCModuleName(module);
                    if (StringUtils.isNotBlank(moduleName)) {//判定是nc模块
                        moduleList.add(module.getName());
                    }
                }
            }
        }
        //排序
        Collections.sort(moduleList);
        //获取模块配置
        Set<String> mustModuleSet = new HashSet<>();
        Set<String> exModuleSet = new HashSet<>();
        if (StringUtils.isBlank(mustModuleStr)) {
            mustModuleSet = ModuleFileUtil.getMustMoudleSet();
        } else {
            String[] strings = mustModuleStr.split(",");
            mustModuleSet.addAll(Arrays.asList(strings));
        }
        if (StringUtils.isNotBlank(selModuleStr)) {
            String[] strings = selModuleStr.split(",");
            exModuleSet.addAll(Arrays.asList(strings));
        }
        List<Object[]> mustRows = new ArrayList<>(moduleList.size());
        List<Object[]> selectedRows = new ArrayList<>(moduleList.size());
        int i = 1;
        for (String str : moduleList) {
            boolean checked = mustModuleSet.contains(str);
            mustRows.add(new Object[]{i, checked, str});
            checked = exModuleSet.contains(str);
            selectedRows.add(new Object[]{i, !checked, str});
            i++;
        }
        return new ModuleTableData(mustRows, selectedRows);
    }

    public record ModuleTableData(List<Object[]> mustRows, List<Object[]> selectedRows) {
    }

    /**
     * nc 模块名称
     *
     * @param module module
     * @return String
     */
    private static String getNCModuleName(File module) {
        String ncModuleName = null;
        String moduleFilePath =
                module.getPath() + File.separator + "META-INF" + File.separator + "module.xml";
        try {
            File file = new File(moduleFilePath);
            if (file.exists()) {
                Document doc;
                try (InputStream in = new FileInputStream(file)) {
                    doc = XmlUtil.parse(in);
                }
                Element root = doc.getDocumentElement();
                ncModuleName = root.getAttribute("name");
            }
        } catch (Exception e) {
            //抛错就认为不是nc项目
        }
        return ncModuleName;
    }

    public static void setAllCheckState(JTable table, boolean checked) {
        if (table.getModel() instanceof BulkTableModel bulkTableModel) {
            bulkTableModel.setBooleanColumn(1, checked);
            return;
        }
        int rowCount = table.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            table.getModel().setValueAt(checked, i, 1);
        }
    }

    /**
     * 更新模块选择内容
     *
     * @param dialog dialog
     * @throws BusinessException BusinessException
     */
    public static void saveModuleConfig(AbstractDialog dialog) throws BusinessException {
        UapProjectEnvironment environment = UapProjectEnvironment.getInstance(
                dialog.getProjectContext());
        String oldMust = environment.getMust_modules();
        String oldEx = environment.getEx_modules();
        DevConfigDialog view = (DevConfigDialog) dialog;
        JTable selTable = view.selectedModulesTable();
        JTable mustTable = view.requiredModulesTable();
        ModuleSelection selection = collectModuleSelection(
                mustTable.getModel(), selTable.getModel());
        if (!Objects.equals(Objects.toString(oldMust, ""), selection.mustModules())) {
            environment.setMust_modules(selection.mustModules());
        }
        if (!Objects.equals(Objects.toString(oldEx, ""), selection.excludedModules())) {
            environment.setEx_modules(selection.excludedModules());
            CreatApplicationConfigurationUtil.update(dialog.getProjectContext());
        }
    }

    static ModuleSelection collectModuleSelection(TableModel mustModel,
                                                   TableModel selectedModel) {
        Set<String> mustNames = new HashSet<>();
        List<String> mustModules = new ArrayList<>();
        for (int row = 0; row < mustModel.getRowCount(); row++) {
            if (Boolean.TRUE.equals(mustModel.getValueAt(row, 1))) {
                String moduleName = String.valueOf(mustModel.getValueAt(row, 2));
                mustNames.add(moduleName);
                mustModules.add(moduleName);
            }
        }
        List<String> excludedModules = new ArrayList<>();
        for (int row = 0; row < selectedModel.getRowCount(); row++) {
            String moduleName = String.valueOf(selectedModel.getValueAt(row, 2));
            boolean selected = Boolean.TRUE.equals(selectedModel.getValueAt(row, 1));
            if (!selected && !mustNames.contains(moduleName)) {
                excludedModules.add(moduleName);
            }
        }
        return new ModuleSelection(String.join(",", mustModules),
                String.join(",", excludedModules));
    }

    record ModuleSelection(String mustModules, String excludedModules) {
    }
}
