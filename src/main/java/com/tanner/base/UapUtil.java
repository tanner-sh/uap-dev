package com.tanner.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;

public class UapUtil {

  public static final String getUapVersion(String ncHomePath) {
    String ncscriptPath = ncHomePath + File.separator + "ncscript";
    File versionFile = new File(ncscriptPath, "uapServer" + File.separator + "setup.ini");
    if (!versionFile.exists()) {
      versionFile = new File(ncscriptPath, "uap" + File.separator + "setup.ini");
      if (!versionFile.exists()) {
        return "unknown";
      }
    }
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
    String version = (String) properties.get("version");
    if (StringUtils.isBlank(version)) {
      return "unknown";
    }
    version = StringUtils.deleteWhitespace(version).toLowerCase();
    version = version.replaceAll("\\.", "");
    if (Pattern.matches("[0-9].*", version)) {
      return "nc" + version.substring(0, 2);
    } else if (version.startsWith("nccloud")) {
      if (version.length() >= 13) {
        version = version.substring(0, 13);
      }
      return version;
    }
    return version;
  }

}

