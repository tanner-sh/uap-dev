package com.tanner.base;

import com.tanner.devconfig.util.AESEncode;
import com.tanner.devconfig.util.Encode;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Pattern;

public final class UapUtil {

    private static Encode encode = new Encode();

    /**
     * 根据home获取home版本
     *
     * @param ncHomePath
     * @return
     */
    public static String getUapVersion(String ncHomePath) {
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
                version = version.substring(0, 7) + version.substring(9, 13);
            }
            return version.replace("nccloud", "ncc");
        }
        return version;
    }

    /**
     * 解密数据源密码
     *
     * @param homePath
     * @param text
     * @return
     */
    public static String decodeDbPwd(String text, String homePath) {
        File propFile = new File(homePath, "/ierp/bin/key.properties");
        if (propFile.exists()) {
            return AESEncode.decrypt(text, homePath);
        } else {
            return encode.decode(text);
        }
    }

    /**
     * 加密数据源密码
     *
     * @param homePath
     * @param text
     * @return
     */
    public static String encodeDbPwd(String text, String homePath) {
        File propFile = new File(homePath, "/ierp/bin/key.properties");
        if (propFile.exists()) {
            return AESEncode.encrypt(text, homePath);
        } else {
            return encode.encode(text);
        }
    }

}

