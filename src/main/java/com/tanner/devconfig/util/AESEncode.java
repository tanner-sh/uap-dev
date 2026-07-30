package com.tanner.devconfig.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * AES compatibility codec used by NC datasource configuration.
 */
public final class AESEncode {

    private static final IvParameterSpec IV =
            new IvParameterSpec("1234567890123456".getBytes(StandardCharsets.UTF_8));
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String PREFIX = "#";

    private AESEncode() {
    }

    public static String encrypt(String data, String homePath) {
        if (data == null) {
            return null;
        }
        try {
            byte[] encrypted = crypt(Cipher.ENCRYPT_MODE, data.getBytes(StandardCharsets.UTF_8),
                    getOrCreateKey(homePath));
            return PREFIX + parseByte2HexStr(encrypted);
        } catch (Exception e) {
            throw new IllegalStateException("加密数据源密码失败: " + e.getMessage(), e);
        }
    }

    public static String decrypt(String data, String homePath) {
        if (data == null || !data.startsWith(PREFIX) || data.length() == 1) {
            throw new IllegalArgumentException("非法的 AES 密文");
        }
        try {
            String key = query(homePath);
            if (key == null) {
                throw new IllegalStateException("找不到数据源密钥文件");
            }
            byte[] decoded = crypt(Cipher.DECRYPT_MODE, parseHexStr2Byte(data.substring(1)), key);
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("解密数据源密码失败: " + e.getMessage(), e);
        }
    }

    public static String aesEncode(String text, String homePath) {
        String encrypted = encrypt(text, homePath);
        return encrypted == null ? null : encrypted.substring(1);
    }

    public static String aesDecode(String encodedString, String homePath) {
        return decrypt(PREFIX + encodedString, homePath);
    }

    private static byte[] crypt(int mode, byte[] input, String key) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(mode, new SecretKeySpec(parseHexStr2Byte(key), "AES"), IV);
        return cipher.doFinal(input);
    }

    public static String parseByte2HexStr(byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("字节数组不能为空");
        }
        return java.util.HexFormat.of().withUpperCase().formatHex(bytes);
    }

    public static byte[] parseHexStr2Byte(String hex) {
        if (hex == null || hex.isEmpty() || (hex.length() & 1) != 0
                || !hex.matches("[0-9a-fA-F]+")) {
            throw new IllegalArgumentException("非法的十六进制数据");
        }
        return java.util.HexFormat.of().parseHex(hex);
    }

    public static String query(String ncHome) {
        Path keyFile = resolveKeyFile(ncHome);
        if (!Files.isRegularFile(keyFile)) {
            return null;
        }
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(keyFile)) {
            properties.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("读取数据源密钥失败: " + keyFile, e);
        }
        String key = properties.getProperty("secret_key");
        return key == null || key.isBlank() ? null : key.trim();
    }

    public static void insert(String secretKey, String ncHome) {
        Path keyFile = resolveKeyFile(ncHome);
        Properties properties = new Properties();
        properties.setProperty("secret_key", secretKey);
        try {
            Files.createDirectories(keyFile.getParent());
            try (OutputStream output = Files.newOutputStream(keyFile)) {
                properties.store(output, null);
            }
        } catch (IOException e) {
            throw new IllegalStateException("写入数据源密钥失败: " + keyFile, e);
        }
    }

    private static String getOrCreateKey(String homePath) throws IOException {
        String key = query(homePath);
        if (key != null) {
            return key;
        }
        String generated = parseByte2HexStr(AESGeneratorKey.genBindIpKey());
        insert(generated, homePath);
        return generated;
    }

    private static Path resolveKeyFile(String ncHome) {
        if (ncHome == null || ncHome.isBlank()) {
            throw new IllegalArgumentException("NC Home 不能为空");
        }
        return Path.of(ncHome).toAbsolutePath().normalize()
                .resolve("ierp").resolve("bin").resolve("key.properties");
    }
}
