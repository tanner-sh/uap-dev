package com.tanner.langsearch;

public class LangInfo {

    int lineNumber; // 内部行号
    String path; // 文件路径
    String internalPath; // jar包内部路径
    String language;// 语言
    String text;// 匹配文本

    public LangInfo(int lineNumber, String path, String internalPath, String language, String text) {
        this.lineNumber = lineNumber;
        this.path = path;
        this.internalPath = internalPath;
        this.language = language;
        this.text = text;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getInternalPath() {
        return internalPath;
    }

    public void setInternalPath(String internalPath) {
        this.internalPath = internalPath;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "LangInfo{\n" +
                "            lineNumber=" + lineNumber + "\n" +
                ",             path='" + path + '\'' + "\n" +
                ",             internalPath='" + internalPath + '\'' + "\n" +
                ",             language='" + language + '\'' + "\n" +
                ",             text='" + text + '\'' + "\n" +
                '}' + "\n";
    }
}
