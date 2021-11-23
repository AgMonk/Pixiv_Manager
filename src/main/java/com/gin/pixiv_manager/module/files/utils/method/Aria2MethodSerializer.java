package com.gin.pixiv_manager.module.files.utils.method;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;

import java.lang.reflect.Type;

/**
 * 枚举对象的序列化方法
 *
 * @author bx002
 * @date 2021/2/3 15:42
 */
public class Aria2MethodSerializer implements ObjectSerializer {
    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) {
        Aria2Method method = (Aria2Method) object;
        serializer.write(method.getName());
    }
}
