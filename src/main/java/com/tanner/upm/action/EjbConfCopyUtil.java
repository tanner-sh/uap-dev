package com.tanner.upm.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.tanner.base.BaseUtil;
import com.tanner.base.UapProjectEnvironment;
import com.tanner.base.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EjbConfCopyUtil {

    /**
     * 递归路径获取可导出的文件
     *
     * @param filePath filePath
     */
    private Set<String> getFileUrl(String filePath) {
        Set<String> fileUrlSet = new HashSet<>();
        File file = new File(filePath);
        if (file.isDirectory()) {
            File[] childrenFile = file.listFiles();
            if (childrenFile == null) {
                return fileUrlSet;
            }
            for (File childFile : childrenFile) {
                fileUrlSet.addAll(getFileUrl(childFile.getPath()));
            }
        } else {
            if ((filePath.endsWith(".rest") || filePath.endsWith(".upm")) && new File(
                    filePath).getParent().endsWith("META-INF")) {
                fileUrlSet.add(filePath);
            }
        }
        return fileUrlSet;
    }

    /**
     * 只拷贝选中的模块的upm文件到home
     *
     * @param event event
     */
    public int copy(AnActionEvent event) throws Exception {
        String homePath = UapProjectEnvironment.getInstance(event.getProject()).getUapHomePath();
        if (homePath.isBlank()) {
            throw new IllegalArgumentException("Not set NC Home");
        }
        Module module = BaseUtil.getModule(event);
        String ncModuleName = getNCModuleName(module);
        if (ncModuleName == null || ncModuleName.isBlank()) {
            throw new IllegalArgumentException("Can't determine NC module name");
        }
        VirtualFile selected = event.getData(CommonDataKeys.VIRTUAL_FILE);
        if (selected == null) {
            throw new IllegalArgumentException("请选择 UPM/REST 文件或目录");
        }
        // 目标路径，但是缺少文件名字
        String toPath =
                homePath + File.separator + "modules" + File.separator + ncModuleName + File.separator
                        + "META-INF" + File.separator;
        Set<String> fileUrls = getFileUrl(selected.getPath());
        List<String> errorList = new ArrayList<>();
        int copied = 0;
        for (String fileUrl : fileUrls) {
            File file = new File(fileUrl);
            try {
                // 逐个拷贝到home
                FileUtil.copy(file, new File(toPath + file.getName()));
                copied++;
            } catch (IOException ignored) {
                errorList.add(file.getName());
            }
        }
        if (!errorList.isEmpty()) {
            throw new IOException("文件" + errorList + "拷贝出错");
        }
        return copied;
    }

    /**
     * 获取nc模块名称
     *
     * @param module
     * @return
     */
    private String getNCModuleName(Module module) {
        String ncModuleName = null;
        VirtualFile virtualFile = module.getModuleFile();
        if (virtualFile == null) {
            return null;
        }
        String modulePath = virtualFile.getParent().getPath();
        try {
            File file = new File(
                    modulePath + File.separator + "META-INF" + File.separator + "module.xml");
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
}
