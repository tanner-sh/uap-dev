package com.tanner.devconfig.util;

import com.intellij.openapi.diagnostic.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Properties;

/**
 * copy from home
 */
public class AESEncode {
    static final IvParameterSpec iv = new IvParameterSpec(getUTF8Bytes("1234567890123456"));
    static final String transform = "AES/CBC/PKCS5Padding";
    private static final Logger LOG = Logger.getInstance(AESEncode.class);
    private static final String FLAY = "#";
    private static String KEY = null;

    public AESEncode() {
    }

    public static String encrypt(String data, String homePath) {
        return "#" + aesEncode(data, homePath);
    }

    public static String decrypt(String data, String homePath) {
        data = data.substring(1);
        return aesDecode(data, homePath);
    }

    public static String aesEncode(String text, String homePath) {
        new Properties();
        String encodedString = null;
        Security.addProvider(new BouncyCastleProvider());
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            Throwable localThrowable3 = null;
            try {
                ByteBuffer inBuffer = ByteBuffer.allocateDirect(1024);
                ByteBuffer outBuffer = ByteBuffer.allocateDirect(1024);
                inBuffer.put(getUTF8Bytes(text));
                inBuffer.flip();
                SecretKeySpec keySpec = null;
                if (query(homePath) != null) {
                    keySpec = new SecretKeySpec(parseHexStr2Byte(query(homePath)), "AES");
                } else {
                    byte[] keysecByte = AESGeneratorKey.genBindIpKey();
                    insert(parseByte2HexStr(keysecByte), homePath);
                    keySpec = new SecretKeySpec(keysecByte, "AES");
                }
                cipher.init(1, keySpec, iv);
                int updateBytes = cipher.update(inBuffer, outBuffer);
                int finalBytes = cipher.doFinal(inBuffer, outBuffer);
                outBuffer.flip();
                byte[] encoded = new byte[updateBytes + finalBytes];
                outBuffer.duplicate().get(encoded);
                encodedString = parseByte2HexStr(encoded);
            } catch (Throwable var14) {
                localThrowable3 = var14;
                throw var14;
            } finally {
                if (cipher != null && localThrowable3 != null) {
                }
            }
        } catch (Exception var16) {
        }
        return encodedString;
    }

    public static String aesDecode(String encodedString, String homePath) {
        Security.addProvider(new BouncyCastleProvider());
        new Properties();
        ByteBuffer decoded = ByteBuffer.allocateDirect(1024);
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            Throwable localThrowable4 = null;
            try {
                SecretKeySpec keySpec = null;
                if (query(homePath) != null) {
                    keySpec = new SecretKeySpec(parseHexStr2Byte(query(homePath)), "AES");
                } else {
                    byte[] keysecByte = AESGeneratorKey.genBindIpKey();
                    insert(parseByte2HexStr(keysecByte), homePath);
                    keySpec = new SecretKeySpec(keysecByte, "AES");
                }
                cipher.init(2, keySpec, iv);
                ByteBuffer outBuffer = ByteBuffer.allocateDirect(1024);
                outBuffer.put(parseHexStr2Byte(encodedString));
                outBuffer.flip();
                cipher.update(outBuffer, decoded);
                cipher.doFinal(outBuffer, decoded);
                decoded.flip();
            } catch (Throwable var10) {
                localThrowable4 = var10;
                throw var10;
            } finally {
                if (cipher != null && localThrowable4 != null) {
                }
            }
        } catch (Throwable var12) {
            LOG.error(var12);
            return null;
        }
        return asString(decoded);
    }

    public static String parseByte2HexStr(byte[] buf) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < buf.length; ++i) {
            String hex = Integer.toHexString(buf[i] & 255);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1) {
            return null;
        } else {
            byte[] result = new byte[hexStr.length() / 2];
            for (int i = 0; i < hexStr.length() / 2; ++i) {
                int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
                int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
                result[i] = (byte) (high * 16 + low);
            }
            return result;
        }
    }

    private static byte[] getUTF8Bytes(String input) {
        return input.getBytes(StandardCharsets.UTF_8);
    }

    private static String asString(ByteBuffer buffer) {
        ByteBuffer copy = buffer.duplicate();
        byte[] bytes = new byte[copy.remaining()];
        copy.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static String query(String ncHome) {
        if (KEY != null) {
            return KEY;
        } else {
            if (ncHome.contains("bin")) {
                ncHome = ncHome.split("bin")[0];
                ncHome = ncHome.substring(0, ncHome.length() - 1);
            }
            Properties properties = new Properties();
            File propFile = new File(ncHome, "/ierp/bin/key.properties");
            FileInputStream fileStream = null;
            if (propFile.exists()) {
                String var6;
                try {
                    fileStream = new FileInputStream(propFile);
                    properties.load(fileStream);
                    KEY = properties.get("secret_key").toString();
                    var6 = KEY;
                } catch (Exception var14) {
                    LOG.error("Query the secret_key properties error!", var14);
                    return null;
                } finally {
                    try {
                        if (fileStream != null) {
                            fileStream.close();
                        }
                    } catch (IOException var13) {
                        LOG.error("close io exception ", var13);
                    }

                }
                return var6;
            } else {
                return null;
            }
        }
    }

    public static void insert(String secret_key, String ncHome) {
        if (ncHome.contains("bin")) {
            ncHome = ncHome.split("bin")[0];
            ncHome = ncHome.substring(0, ncHome.length() - 1);
        }
        File propFile = new File(ncHome, "/ierp/bin/key.properties");
        Properties properties = new Properties();
        properties.setProperty("secret_key", secret_key);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(propFile);
            properties.store(outputStream, (String) null);
            KEY = secret_key;
            return;
        } catch (Exception var14) {
            LOG.error("Write the secret_key properties error!", var14);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException var13) {
                    LOG.error("close io exception ", var13);
                }
            }
        }
    }

}

