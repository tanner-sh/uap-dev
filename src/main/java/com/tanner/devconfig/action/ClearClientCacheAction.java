package com.tanner.devconfig.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.tanner.abs.AbstractAnAction;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;

/**
 * 清除客户端缓存
 */
public class ClearClientCacheAction extends AbstractAnAction {

  @Override
  public void doAction(AnActionEvent event) {
    String cacheDirPath = System.getProperty("user.home") + File.separator + "NCCACHE";
    File cacheDir = new File(cacheDirPath);
    if (cacheDir.exists()) {
      try {
        FileUtils.deleteDirectory(cacheDir);
      } catch (IOException e) {
        Messages.showInfoMessage("删除文件异常!\n" + e.getMessage(), "错误");
        return;
      }
    }
    Messages.showInfoMessage("清除完毕", "清除完毕");
  }
}
