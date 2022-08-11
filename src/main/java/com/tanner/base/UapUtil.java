package com.tanner.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class UapUtil {

  public static final String getUapVersion(String ncHomePath) {
    String ncscriptPath = ncHomePath + File.separator + "ncscript";
    File versionFile = new File(ncscriptPath, "uapServer" + File.separator + "setup.ini");
    if (versionFile.exists()) {
      Properties properties = new Properties();
      FileInputStream fis = null; // 读
      try {
        fis = new FileInputStream(versionFile);
        properties.load(fis);
      } catch (IOException e) {
        return null;
      } finally {
        try {
          fis.close();
        } catch (IOException e) {
        }
      }
      return (String) properties.get("version");
    }
    return null;
  }
}
