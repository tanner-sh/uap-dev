package com.tanner.devconfig.util;

import javax.crypto.KeyGenerator;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;

/**
 * copy from home
 */
public class AESGeneratorKey {
    private static final byte[] KEY_END = new byte[]{43, 65, 23, 6, -54, -24, -16, 26, 7, 34, -29, -52, -14, 27, 38, 41};
    private static final int AESKEY_LENGTH = 256;
    private static final int TRANS_KEY_LENGTH = 32;
    private static final byte[] DEFAULT_TRANS_KEY = new byte[]{34, 25, 64, 23, 54, 65, 76, 34, -3, -54, -13, -35, 34, 54, 23};

    public AESGeneratorKey() {
    }

    public static byte[] genKey() throws IOException {
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(AESKEY_LENGTH, new SecureRandom());
            return generator.generateKey().getEncoded();
        } catch (Exception exception) {
            throw new IOException("gen key error", exception);
        }
    }

    public static byte[] genKey(byte[] transKey) throws IOException {
        byte[] key = new byte[32];
        System.arraycopy(transKey, 8, key, 0, 24);
        System.arraycopy(KEY_END, 0, key, 24, 8);

        return key;
    }

    public static byte[] genTransKey() {
        String ClientIP = null;
        byte[] transKey = new byte[32];

        try {
            ClientIP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException var4) {
        }

        if (ClientIP == null || ClientIP.equals("")) {
            ClientIP = "uap:localHost";
        }

        byte[] srcKey = ClientIP.getBytes(StandardCharsets.UTF_16);
        if (srcKey.length > TRANS_KEY_LENGTH) {
            System.arraycopy(srcKey, 0, transKey, 0, TRANS_KEY_LENGTH);
        } else {
            System.arraycopy(srcKey, 0, transKey, 0, srcKey.length);
        }
        return transKey;
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

    public static String generHexStrKey() {
        try {
            return parseByte2HexStr(genKey());
        } catch (IOException exception) {
            throw new IllegalStateException("生成 AES 密钥失败", exception);
        }
    }

    public static byte[] genBindIpKey() throws IOException {
        return genKey(genTransKey());
    }
}
