package com.tanner.devconfig.util;

import com.tanner.abs.AbstractDialog;
import com.tanner.base.BusinessException;
import com.tanner.base.ModuleFileUtil;
import com.tanner.base.UapProjectEnvironment;
import com.tanner.debug.util.CreatApplicationConfigurationUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TableModelUtil {

  /**
   * 必选模块面板
   **/
  public final static int MODULE_TYPE_MUST = 0;

  /**
   * 启动模块面板
   **/
  public final static int MODULE_TYPE_SEL = 1;

  public static DefaultTableModel getMustModel(AbstractDialog dialog) {
    DefaultTableModel mustModel = new DefaultTableModel(null,
        new String[]{"NO.", "Checked", "moduleName"}) {
      public Class getColumnClass(int c) {
        switch (c) {
          case 0:
            return Integer.class;
          case 1:
            return Boolean.class;
          default:
            return String.class;
        }
      }

      //第二列不允许编辑
      public boolean isCellEditable(int row, int column) {
        boolean flag = false;
        Object obj = dialog.getComponent(JTable.class, "mustTable").getValueAt(row, 2);
        if (column == 1 && !ModuleFileUtil.getMustMoudleSet().contains(obj)) {
          flag = true;
        }
        return flag;
      }
    };
    return mustModel;
  }

  public static DefaultTableModel getSelModel(AbstractDialog dialog) {
    DefaultTableModel model = new DefaultTableModel(null,
        new String[]{"NO.", "Checked", "moduleName"}) {
      public Class getColumnClass(int c) {
        switch (c) {
          case 0:
            return Integer.class;
          case 1:
            return Boolean.class;
          default:
            return String.class;
        }
      }

      //第二列不允许编辑
      public boolean isCellEditable(int row, int column) {
        return column == 1;
      }
    };
    return model;
  }

  public static void modelHandle(AbstractDialog dialog, DefaultTableModel mustModel,
      DefaultTableModel selModel) {
    String homePath = UapProjectEnvironment.getInstance().getUapHomePath();
    if (StringUtils.isBlank(homePath)) {
      homePath = dialog.getComponent(JTextField.class, "homeText").getText();
    }
    if (StringUtils.isBlank(homePath)) {
      return;
    }
    //扫描所有module
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
    String mustModuleStr = UapProjectEnvironment.getInstance().getMust_modules();
    String selModuleStr = UapProjectEnvironment.getInstance().getEx_modules();
    Set<String> mustModuleSet = new HashSet();
    Set<String> exModuleSet = new HashSet();
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
    int i = 1;
    for (String str : moduleList) {
      //处理必选模块
      boolean checked = mustModuleSet.contains(str);
      Vector vm = new Vector();
      vm.add(i);
      vm.add(checked);
      vm.add(str);
      mustModel.addRow(vm);
      //处理上次选择的启动模块
      checked = exModuleSet.contains(str);
      Vector vs = new Vector();
      vs.add(i);
      vs.add(!checked);//这里要取反，因为保存的是不启动的模块
      vs.add(str);
      selModel.addRow(vs);
      i++;
    }
  }

  /**
   * nc 模块名称
   *
   * @param module
   * @return
   */
  private static String getNCModuleName(File module) {
    String ncModuleName = null;
    String moduleFilePath =
        module.getPath() + File.separator + "META-INF" + File.separator + "module.xml";
    try {
      File file = new File(moduleFilePath);
      if (file.exists()) {
        InputStream in = new FileInputStream(file);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(in);
        Element root = doc.getDocumentElement();
        ncModuleName = root.getAttribute("name");
      }
    } catch (Exception e) {
      //抛错就认为不是nc项目
    }
    return ncModuleName;
  }

  public static void setAllCheckState(JTable table, boolean checked) {
    int rowCount = table.getRowCount();
    for (int i = 0; i < rowCount; i++) {
      table.getModel().setValueAt(checked, i, 1);
    }
  }

  /**
   * 更新模块选择内容
   *
   * @param dialog
   * @throws BusinessException
   */
  public static void saveModuleConfig(AbstractDialog dialog) throws BusinessException {
    String oldMust = UapProjectEnvironment.getInstance().getMust_modules();
    String oldEx = UapProjectEnvironment.getInstance().getEx_modules();
    JTable selTable = dialog.getComponent(JTable.class, "selTable");
    JTable mustTable = dialog.getComponent(JTable.class, "mustTable");
    int rowCount = selTable.getRowCount();
    StringBuilder mustModuleStr = new StringBuilder();
    StringBuilder exModuleStr = new StringBuilder();
    for (int i = 0; i < rowCount; i++) {
      boolean mustFlag = (boolean) mustTable.getValueAt(i, 1);
      boolean selFlag = (boolean) selTable.getValueAt(i, 1);
      if (mustFlag) {
        String name = mustTable.getValueAt(i, 2).toString();
        mustModuleStr.append(",").append(name);
      }
      if (!selFlag && !mustFlag) {//把没有选择启动的模块以及非必选的模块放在这里
        String name = selTable.getValueAt(i, 2).toString();
        exModuleStr.append(",").append(name);
      }
    }
    if (mustModuleStr.length() > 1) {
      mustModuleStr = new StringBuilder(mustModuleStr.substring(1));
    }
    if (exModuleStr.length() > 1) {
      exModuleStr = new StringBuilder(exModuleStr.substring(1));
    }
    if (!oldMust.contentEquals(mustModuleStr)) {
      UapProjectEnvironment.getInstance().setMust_modules(mustModuleStr.toString());
    }
    if (!oldEx.contentEquals(exModuleStr)) {
      UapProjectEnvironment.getInstance().setEx_modules(exModuleStr.toString());
      CreatApplicationConfigurationUtil.update();
    }
  }
}
