package com.tanner.patcher.action;

import com.tanner.base.BusinessException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 * 补丁包压缩工具
 */
public class ZipUtil {

    private static final int BUFFER = 8192;


    public static String toZip(String exportPath, String patchName) throws BusinessException {
        Path sourcePath = Path.of(exportPath).toAbsolutePath().normalize();
        Path parent = sourcePath.getParent();
        if (parent == null) {
            throw new BusinessException("zip failed: invalid export path");
        }
        String fileName = sourcePath.getFileName().toString();
        Path zipPath = parent.resolve(fileName + "_" + patchName + ".zip").normalize();
        if (!zipPath.getParent().equals(parent)) {
            throw new BusinessException("zip failed: invalid patch name");
        }
        File file = sourcePath.toFile();
        try (FileOutputStream fileOutputStream = new FileOutputStream(zipPath.toFile());
             CheckedOutputStream cos = new CheckedOutputStream(fileOutputStream, new CRC32());
             ZipOutputStream out = new ZipOutputStream(cos)) {
            compress(file, out, sourcePath);
        } catch (Exception e) {
            throw new BusinessException("zip failed : " + e.getMessage());
        }
        return zipPath.toString();
    }

    private static void compress(File file, ZipOutputStream out, Path basePath) throws IOException {
        /* 判断是目录还是文件 */
        if (file.isDirectory()) {
            compressDirectory(file, out, basePath);
        } else {
            compressFile(file, out, basePath);
        }
    }

    /**
     * 压缩目录
     *
     * @param dir      dir
     * @param out      out
     * @param basePath basePath
     */
    private static void compressDirectory(File dir, ZipOutputStream out, Path basePath)
            throws IOException {
        if (!dir.exists()) {
            return;
        }
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                /* 递归 */
                compress(file, out, basePath);
            }
        }
    }

    /**
     * 压缩文件
     *
     * @param file     file
     * @param out      out
     * @param basePath basePath
     */
    private static void compressFile(File file, ZipOutputStream out, Path basePath)
            throws IOException {
        if (!file.exists()) {
            return;
        }
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            String filePath = basePath.relativize(file.toPath().toAbsolutePath().normalize())
                    .toString().replace(File.separatorChar, '/');
            ZipEntry entry = new ZipEntry(filePath);
            out.putNextEntry(entry);
            int count;
            byte[] data = new byte[BUFFER];
            while ((count = bis.read(data, 0, BUFFER)) != -1) {
                out.write(data, 0, count);
            }
            out.closeEntry();
        }
    }

}
