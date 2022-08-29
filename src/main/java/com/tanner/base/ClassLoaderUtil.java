package com.tanner.base;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;

public class ClassLoaderUtil {

  public static ClassLoader getUapJdbcClassLoader(String homePath) throws BusinessException {
    //加载home/lib下所有jar包
    URLClassLoader loader = null;
    File driverLibDir = new File(homePath, "driver");
    if (!driverLibDir.exists()) {
      throw new BusinessException("驱动目录不存在!");
    }
    Collection<File> files = FileUtils.listFiles(driverLibDir, null, true);
    if (CollectionUtils.isEmpty(files)) {
      throw new BusinessException("驱动目录中没有驱动!");
    }
    URL[] urls = null;
    try {
      urls = FileUtils.toURLs(files.toArray(new File[0]));
    } catch (IOException e) {
      throw new BusinessException("读取驱动失败!" + e.getMessage());
    }
    return new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
  }

}
