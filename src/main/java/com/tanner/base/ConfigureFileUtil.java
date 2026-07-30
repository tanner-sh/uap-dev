package com.tanner.base;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class ConfigureFileUtil {

    /**
     * 读取输入模版
     *
     * @param fileName fileName
     * @return String
     */
    public String readTemplate(String fileName) throws BusinessException {
        InputStream in = this.getClass().getResourceAsStream("../../../template/" + fileName);
        return readTemplate(in);
    }

    /**
     * 读取输入模板
     *
     * @param file file
     * @return String
     * @throws BusinessException BusinessException
     */
    public String readTemplate(File file) throws BusinessException {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            return readTemplate(fileInputStream);
        } catch (FileNotFoundException e) {
            throw new BusinessException(e.getMessage());
        }
    }

    private String readTemplate(InputStream in) throws BusinessException {
        StringBuilder tempBuilder = new StringBuilder();
        try {
            InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            String lineTxt;
            while ((lineTxt = br.readLine()) != null) {
                tempBuilder.append(lineTxt);
                tempBuilder.append("\r\n");
            }
            br.close();
        } catch (IOException e) {
            throw new BusinessException(e.getMessage());
        }
        return tempBuilder.toString();
    }

    /**
     * 输出文件
     *
     * @param file    file
     * @param content content
     */
    public void outFile(File file, String content, String charset, boolean bomFlag)
            throws BusinessException {
        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                throw new IOException("无法创建目录: " + parent.getPath());
            }
            FileOutputStream fos = new FileOutputStream(file);
            if (bomFlag) {
                fos.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
            }
            try (OutputStreamWriter writer = new OutputStreamWriter(fos, charset)) {
                writer.write(content);
            }
        } catch (IOException e) {
            throw new BusinessException("写入文件失败: " + file.getPath() + "\n" + e.getMessage());
        }
    }
}
