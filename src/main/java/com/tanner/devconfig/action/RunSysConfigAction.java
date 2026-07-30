package com.tanner.devconfig.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.tanner.abs.AbstractAnAction;
import com.tanner.base.UapProjectEnvironment;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

/**
 * 运行sysConfig
 */
public class RunSysConfigAction extends AbstractAnAction {

    @Override
    public void doAction(AnActionEvent event) {
        String ncHomePath = UapProjectEnvironment.getInstance(event.getProject()).getUapHomePath();
        if (StringUtils.isBlank(ncHomePath)) {
            Messages.showErrorDialog("Not set uap home , please check!", "错误");
            return;
        }
        boolean exists = new File(ncHomePath).exists();
        if (!exists) {
            Messages.showErrorDialog("uap home设置错误，请检查!\n" + ncHomePath, "错误");
            return;
        }
        String osName = System.getProperty("os.name");
        try {
            File script;
            ProcessBuilder processBuilder;
            if (osName.startsWith("Windows")) {
                script = new File(ncHomePath, "bin" + File.separator + "sysConfig.bat");
                processBuilder = new ProcessBuilder("cmd.exe", "/c", script.getAbsolutePath());
            } else if (osName.startsWith("Mac") || osName.startsWith("Linux")) {
                script = new File(ncHomePath, "bin" + File.separator + "sysConfig.sh");
                processBuilder = new ProcessBuilder("sh", script.getAbsolutePath());
            } else {
                Messages.showInfoMessage("不支持的操作系统：" + osName, "提示");
                return;
            }
            if (!script.isFile()) {
                Messages.showErrorDialog("找不到 sysConfig 脚本:\n" + script, "错误");
                return;
            }
            processBuilder.directory(new File(ncHomePath)).start();
        } catch (Exception e) {
            Messages.showErrorDialog("运行异常：" + e.getMessage(), "错误");
        }
    }

}
