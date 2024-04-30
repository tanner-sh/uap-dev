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
        String ncHomePath = UapProjectEnvironment.getInstance().getUapHomePath();
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
            if (osName.startsWith("Windows")) {
                String commandWindows = ncHomePath + File.separator + "bin" + File.separator + "sysConfig.bat";
                Runtime.getRuntime().exec(commandWindows);
            } else if (osName.startsWith("Mac")) {
                String commandMac = ncHomePath + File.separator + "bin" + File.separator + "sysConfig.sh";
                Runtime.getRuntime().exec(commandMac);
            } else {
                Messages.showInfoMessage("不支持的操作系统：" + osName, "提示");
            }
        } catch (Exception e) {
            Messages.showErrorDialog("运行异常：" + e.getMessage(), "错误");
        }
    }

}
