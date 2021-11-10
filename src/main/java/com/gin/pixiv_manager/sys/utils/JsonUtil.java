package com.gin.pixiv_manager.sys.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author bx002
 */
public class JsonUtil {

    public static void printJson(Object obj) {
        System.err.println(prettyJson(obj));
    }

    public static void printJson(String str) {
        System.err.println(prettyJson(JSONObject.parse(str)));
    }

    public static void printJsonNotnull(Object obj) {
        obj = JSONObject.parse(JSONObject.toJSONString(obj));
        System.err.println(prettyJson(obj));
    }

    public static String prettyJson(Object obj) {
        return JSON.toJSONString(obj,
                SerializerFeature.PrettyFormat,
//                SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteDateUseDateFormat
        );
    }

    public static <T> T copy(T obj, Class<T> clazz) {
        return JSONObject.parseObject(JSONObject.toJSONString(obj), clazz);
    }

    public static <T> List<T> copy(Collection<T> source, Class<T> clazz) {
        return source.stream().map(obj -> JSONObject.parseObject(JSONObject.toJSONString(obj), clazz)).collect(Collectors.toList());
    }

}
