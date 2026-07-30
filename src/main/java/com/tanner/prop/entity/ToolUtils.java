package com.tanner.prop.entity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ToolUtils {

    private static final int REGEX_FLAGS = Pattern.CASE_INSENSITIVE;
    private static final String HOST_PATTERN = "(\\[[^]]+]|[^:/;]+)";
    private static final Pattern SQL_SERVER_URL = Pattern.compile(
            "^(.*?//)" + HOST_PATTERN + "(?::(\\d+))?"
                    + "(;(?:databaseName|database)=)([^;]+)(.*)$", REGEX_FLAGS);
    private static final Pattern SLASH_DATABASE_URL = Pattern.compile(
            "^(.*?//)" + HOST_PATTERN + "(?::(\\d+))?/([^?;]+)(.*)$", REGEX_FLAGS);
    private static final Pattern ORACLE_SID_URL = Pattern.compile(
            "^(.*?@)" + HOST_PATTERN + "(?::(\\d+))?:([^:;/?]+)(.*)$", REGEX_FLAGS);
    private static final Pattern ORACLE_SERVICE_URL = Pattern.compile(
            "^(.*?@)" + HOST_PATTERN + "(?::(\\d+))?/([^?;]+)(.*)$", REGEX_FLAGS);
    private static final Pattern DESCRIPTION_HOST = Pattern.compile(
            "(?i)(\\bHOST\\s*=\\s*)([^)]+)");
    private static final Pattern DESCRIPTION_PORT = Pattern.compile(
            "(?i)(\\bPORT\\s*=\\s*)([^)]+)");
    private static final Pattern DESCRIPTION_DATABASE = Pattern.compile(
            "(?i)(\\b(?:SERVICE_NAME|SID)\\s*=\\s*)([^)]+)");

    public static boolean isNumber(String str) {
        return str.matches("\\d+");
    }

    public static boolean isSnNumber(String str) {
        return str.matches("(?m)^3\\d{7}$");
    }

    public static boolean checkIPAddress(String ipAddress) {
        return ipAddress.matches(
                "(?m)^(0|[1-9]\\d?|[0-1]\\d{2}|2[0-4]\\d|25[0-5])\\.(0|[1-9]\\d?|[0-1]\\d{2}|2[0-4]\\d|25[0-5])\\.(0|[1-9]\\d?|[0-1]\\d{2}|2[0-4]\\d|25[0-5])\\.(0|[1-9]\\d?|[0-1]\\d{2}|2[0-4]\\d|25[0-5])$");
    }

    public static boolean is_2length_azAZ09(String str) {
        return str.matches("[YZ][A-Z0-9]");
    }

    public static boolean isChinese(String str) {
        return str.matches("[\\u4e00-\\u9fa5]");
    }

    public static String[] getJDBCInfo(String url) {
        String[] jdbc = new String[3];
        if (url == null) {
            return jdbc;
        }
        Matcher matcher = SQL_SERVER_URL.matcher(url);
        if (matcher.matches()) {
            return parts(matcher.group(2), matcher.group(3), matcher.group(5));
        }
        matcher = SLASH_DATABASE_URL.matcher(url);
        if (matcher.matches()) {
            return parts(matcher.group(2), matcher.group(3), matcher.group(4));
        }
        matcher = ORACLE_SERVICE_URL.matcher(url);
        if (matcher.matches()) {
            return parts(matcher.group(2), matcher.group(3), matcher.group(4));
        }
        matcher = ORACLE_SID_URL.matcher(url);
        if (matcher.matches()) {
            return parts(matcher.group(2), matcher.group(3), matcher.group(4));
        }
        Matcher hostMatcher = DESCRIPTION_HOST.matcher(url);
        Matcher portMatcher = DESCRIPTION_PORT.matcher(url);
        Matcher databaseMatcher = DESCRIPTION_DATABASE.matcher(url);
        if (hostMatcher.find() && portMatcher.find() && databaseMatcher.find()) {
            return parts(hostMatcher.group(2).trim(), portMatcher.group(2).trim(),
                    databaseMatcher.group(2).trim());
        }
        return jdbc;
    }

    public static boolean isJDBCUrl(String url) {
        return url != null && url.regionMatches(true, 0, "jdbc:", 0, 5)
                && !url.regionMatches(true, 0, "jdbc:odbc:", 0, 10);
    }

    public static String getJDBCUrl(String example, String database, String host, String port) {
        if (example == null) {
            return "";
        }
        Matcher matcher = SQL_SERVER_URL.matcher(example);
        if (matcher.matches()) {
            return matcher.group(1) + host + portPart(port, matcher.group(3))
                    + matcher.group(4) + database + matcher.group(6);
        }
        matcher = SLASH_DATABASE_URL.matcher(example);
        if (matcher.matches()) {
            return matcher.group(1) + host + portPart(port, matcher.group(3))
                    + "/" + database + matcher.group(5);
        }
        matcher = ORACLE_SERVICE_URL.matcher(example);
        if (matcher.matches()) {
            return matcher.group(1) + host + portPart(port, matcher.group(3))
                    + "/" + database + matcher.group(5);
        }
        matcher = ORACLE_SID_URL.matcher(example);
        if (matcher.matches()) {
            return matcher.group(1) + host + portPart(port, matcher.group(3))
                    + ":" + database + matcher.group(5);
        }
        if (DESCRIPTION_HOST.matcher(example).find()
                && DESCRIPTION_PORT.matcher(example).find()
                && DESCRIPTION_DATABASE.matcher(example).find()) {
            String result = replaceDescriptorValue(example, DESCRIPTION_HOST, host);
            String descriptorPort = port;
            if (descriptorPort == null || descriptorPort.isBlank()) {
                descriptorPort = getJDBCInfo(example)[1];
            }
            result = replaceDescriptorValue(result, DESCRIPTION_PORT, descriptorPort);
            return replaceDescriptorValue(result, DESCRIPTION_DATABASE, database);
        }
        return example;
    }

    private static String[] parts(String host, String port, String database) {
        return new String[]{host, port == null ? "" : port, database};
    }

    private static String portPart(String requestedPort, String originalPort) {
        String value = requestedPort == null || requestedPort.isBlank()
                ? originalPort : requestedPort;
        return value == null || value.isBlank() ? "" : ":" + value;
    }

    private static String replaceDescriptorValue(String url, Pattern pattern, String value) {
        Matcher matcher = pattern.matcher(url);
        if (!matcher.find()) {
            return url;
        }
        return matcher.replaceFirst(Matcher.quoteReplacement(
                matcher.group(1) + (value == null ? "" : value)));
    }

    public static String getODBCDBName(String url) {
        Pattern regex = Pattern.compile(".*:([^:]+)", 128);
        Matcher matcher = regex.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public static String getODBCUrl(String example, String database) {
        StringBuffer url = new StringBuffer();
        Pattern regex = Pattern.compile("(.*:)([^:]+)", 128);
        Matcher matcher = regex.matcher(example);
        if (matcher.find()) {
            url.append(matcher.group(1));
            url.append(database);
        }
        return url.toString();
    }

}
