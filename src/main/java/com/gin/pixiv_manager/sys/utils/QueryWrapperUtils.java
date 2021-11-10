package com.gin.pixiv_manager.sys.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.util.Collection;

/**
 * 查询条件工具类
 * @author bx002
 */
public class QueryWrapperUtils {

    public static void likeIfNotEmpty(QueryWrapper<?> queryWrapper, String column, String value) {
        if (isEmpty(column) || isEmpty(value)) {
            return;
        }
        queryWrapper.like(column, value);
    }

    public static void inIfNotEmpty(QueryWrapper<?> queryWrapper, String column, Collection<String> value) {
        if (isEmpty(column) || isEmpty(value)) {
            return;
        }
        queryWrapper.in(column, value);
    }

    public static void likeRightIfNotEmpty(QueryWrapper<?> queryWrapper, String column, String value) {
        if (isEmpty(column) || isEmpty(value)) {
            return;
        }
        queryWrapper.likeRight(column, value);
    }

    public static void equalsIfNotEmpty(QueryWrapper<?> queryWrapper, String column, Object value) {
        if (isEmpty(column) || isEmpty(value)) {
            return;
        }
        queryWrapper.eq(column, value);
    }

    public static void betweenIfNotEmpty(QueryWrapper<?> queryWrapper, String column, Long start, Long end) {
        if (isEmpty(column) || isEmpty(start) || isEmpty(end)) {
            return;
        }
        queryWrapper.between(column, start, end);
    }


    private static boolean isEmpty(Object obj) {
        return obj == null || "".equals(obj);
    }

    private static boolean isEmpty(Collection<?> list) {
        return list == null || list.size() == 0;
    }

}
