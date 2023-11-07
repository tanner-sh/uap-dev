package com.tanner;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarReadTest {

    @org.junit.Test
    public void readJar() throws Exception {
        String langLibPath = "/Users/tanner/Documents/yonyou/uaphomes/NCC2005_all_modules/langlib";
        Collection<File> jarFiles = FileUtils.listFiles(new File(langLibPath), new String[]{"jar"}, true);
        String searchValue = "03607not-0227";
        List<LangInfo> matchedLangs = new ArrayList<>();
        Properties properties = new Properties();
        try {
            for (File file : jarFiles) {
                JarFile jarFile = new JarFile(file);
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (entry.getName().endsWith(".properties")) {
                        InputStream is = jarFile.getInputStream(entry);
                        List<String> lines = IOUtils.readLines(is, StandardCharsets.UTF_16BE);
                        lines.stream().filter(line -> line.contains(searchValue))
                                .forEach(line -> {
                                    int lineNumber = lines.indexOf(line) + 1;
                                    String path = file.getPath() + "(" + entry.getName() + ")";
                                    String language = "简体中文";
                                    String text = line;
                                    matchedLangs.add(new LangInfo(lineNumber, path, language, text));
                                });

                        is.close();
                    }
                }
                jarFile.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (LangInfo langInfo : matchedLangs) {
            System.out.println(langInfo);
        }
    }

}

class LangInfo {

    int lineNumber;
    String path;
    String language;
    String text;

    public LangInfo(int lineNumber, String path, String language, String text) {
        this.lineNumber = lineNumber;
        this.path = path;
        this.language = language;
        this.text = text;
    }

    @Override
    public String toString() {
        return "LangInfo{\n" +
                "            lineNumber=" + lineNumber + "\n" +
                ",             path='" + path + '\'' + "\n" +
                ",             language='" + language + '\'' + "\n" +
                ",             text='" + text + '\'' + "\n" +
                '}' + "\n";
    }

}
