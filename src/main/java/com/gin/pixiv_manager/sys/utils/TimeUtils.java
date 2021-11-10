package com.gin.pixiv_manager.sys.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 时间工具类
 * @author bx002
 */
public class TimeUtils {
    public static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter FULL_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    public static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Asia/Shanghai");

    /**
     * 根据格式和时区解析时间字符串到毫秒
     * @param timeString 时间字符串
     * @param formatter  格式
     * @return long
     */
    public static ZonedDateTime parse(String timeString, DateTimeFormatter formatter) {
        return parse(timeString, formatter, DEFAULT_ZONE_ID);
    }

    /**
     * 根据格式和时区解析时间字符串到毫秒
     * @param timeString 时间字符串
     * @return long
     */
    public static ZonedDateTime parse(String timeString) {
        return parse(timeString, DATE_TIME_FORMATTER, DEFAULT_ZONE_ID);
    }

    /**
     * 根据格式和时区解析时间字符串到毫秒
     * @param timeString 时间字符串
     * @param formatter  格式
     * @param zoneId     时区
     * @return long
     */
    public static ZonedDateTime parse(String timeString, DateTimeFormatter formatter, ZoneId zoneId) {
        return ZonedDateTime.ofLocal(LocalDateTime.parse(timeString, formatter), zoneId, null);
    }

    /**
     * 生成耗时字符串
     * @param start 开始毫秒
     * @param end   结束毫秒
     * @return 耗时字符串
     */
    public static String timeCost(long start, long end) {
        long k = 1000L;
        int ten = 10;
        int minute = 60;
        long dur = end - start;

        if (dur < ten * k) {
            return dur + " 毫秒";
        }
        if (dur < minute * k) {
            return String.format("%.1f 秒", 1.0 * dur / k);
        }
        return String.format("%.1f 分", 1.0 * dur / k / minute);
    }
}
