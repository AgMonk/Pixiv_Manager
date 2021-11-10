package com.gin.pixiv_manager.sys.utils;

import java.util.Collection;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串工具类
 * @author bx002
 */
public class StringUtils {

    private static final Pattern HUMP_PATTERN = Pattern.compile("[A-Z]");

    /**
     * 驼峰转下划线
     * @param str  字符串
     * @return 字符串
     */
    public static String humpToLine(String str) {
        Matcher matcher = HUMP_PATTERN.matcher(str);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }



    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().startsWith("win");
    }

    public static boolean isEmpty(Object o) {
        return o == null || "".equals(o);
    }

    public static boolean isEmpty(Collection<?> o) {
        return o == null || o.size() == 0;
    }

    public static String getProjectName(){
        return StringUtils.class.getCanonicalName().split("\\.")[2];
    }

    /**
     * 生成唯一编号
     * @return 唯一编号
     */
    public static String createUuid(){
        return UUID.randomUUID().toString();
    }

    /**
     * 生成短编号
     * @return 短编号
     */
    public static String createShortUuid(){
        return createUuid().substring(0,8);
    }

    public static void main(String[] args) {
        System.out.println("createUuid() = " + createUuid());
    }
}
