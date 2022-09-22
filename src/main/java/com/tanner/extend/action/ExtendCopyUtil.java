package com.tanner.extend.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.tanner.base.BaseUtil;
import com.tanner.base.UapProjectEnvironment;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class ExtendCopyUtil {

    public static final int BUFFER_SIZE = 8192;
    /**
     * 项目的鉴权文件路径
     */
    public static final String PROJECT_CONFIG_FILE_PATH =
            File.separator + "nccloud" + File.separator + "src" + File.separator + "client"
                    + File.separator + "yyconfig";
    /**
     * 模块的鉴权文件路径
     */
    public static final String HOME_CONFIG_FILE_PATH =
            File.separator + "hotwebs" + File.separator + "nccloud" + File.separator + "WEB-INF"
                    + File.separator + "extend" + File.separator + "yyconfig";


    /**
     * 拷贝一个模块的鉴权文件到home中，拷贝方法就是将该模块鉴权文件目录下的所有xml文件拷贝到home中的鉴权文件目录下
     *
     * @param event
     * @throws Exception
     */
    public static void copyToNCHome(AnActionEvent event) throws Exception {
        Module module = BaseUtil.getModule(event);
        String homePath = UapProjectEnvironment.getInstance().getUapHomePath();
        if (StringUtils.isBlank(homePath)) {
            Messages.showMessageDialog("请先设置NC Home", "Error", Messages.getErrorIcon());
            return;
        }
        copyDir(module.getModuleNioFile().getParent().toString() + PROJECT_CONFIG_FILE_PATH,
                homePath + HOME_CONFIG_FILE_PATH);
    }

    /**
     * 将某个文件下下的所有xml文件拷贝到另一个文件夹
     *
     * @param fromDirPath
     * @param toDirPath
     * @throws IOException
     */
    private static void copyDir(String fromDirPath, String toDirPath) throws IOException {
        File file = new File(fromDirPath);
        String[] subFilePaths = file.list();
        if (subFilePaths == null) {
            return;
        }
        if (!(new File(toDirPath)).exists()) {
            if (!(new File(toDirPath)).mkdir()) {
                Messages.showMessageDialog("Home所在的目录不存在或者，你没有操作home所在目录的权限", "Error",
                        Messages.getErrorIcon());
                return;
            }
        }
        // 递归拷贝
        for (String subFilePath : subFilePaths) {
            File file0 = new File(fromDirPath + File.separator + subFilePath);
            if (file0.isDirectory()) {
                copyDir(fromDirPath + File.separator + subFilePath,
                        toDirPath + File.separator + subFilePath);
            } else if (file0.isFile()) {
                copyXmlFile(fromDirPath + File.separator + subFilePath,
                        toDirPath + File.separator + subFilePath);
            }
        }
    }

    /**
     * 复制xml文件
     *
     * @param fromFilePath
     * @param newFilePath
     * @throws IOException
     */
    private static void copyXmlFile(@NotNull String fromFilePath, @NotNull String newFilePath)
            throws IOException {
        if (!fromFilePath.endsWith(".xml")) {
            return;
        }
        File fromFile = new File(fromFilePath);
        File toFile = new File(newFilePath);
        FileUtil.copy(fromFile, toFile);
    }

    public static void main(String[] args) throws IOException {
        copyDir("D://111", "D://222");
    }
}
