package com.tanner.devconfig.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.tanner.abs.AbstractAnAction;
import java.io.File;
import java.io.IOException;

/**
 * 清除客户端缓存
 */
public class ClearClientCacheAction extends AbstractAnAction {

  @Override
  public void doAction(AnActionEvent event) {
    String userHomePath = System.getProperty("user.home") + File.separator + "NCCACHE";
    File userHomeDir = new File(userHomePath);
    if (userHomeDir.exists()) {
      VirtualFile virtualFile = LocalFileSystem.getInstance()
          .refreshAndFindFileByIoFile(userHomeDir);
      try {
        virtualFile.delete(this);
      } catch (IOException e) {
        Messages.showInfoMessage("删除文件异常!\n" + e.getMessage(), "错误");
        return;
      }
    }
    Messages.showInfoMessage("清除完毕", "清除完毕");
  }
}
