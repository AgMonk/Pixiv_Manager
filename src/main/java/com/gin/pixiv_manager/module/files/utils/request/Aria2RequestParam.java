package com.gin.pixiv_manager.module.files.utils.request;

import com.alibaba.fastjson.annotation.JSONField;
import com.gin.pixiv_manager.module.files.utils.method.Aria2Method;
import com.gin.pixiv_manager.module.files.utils.method.Aria2MethodSerializer;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Aria2请求参数
 * @author bx002
 */
@Data
public class Aria2RequestParam implements Serializable {
    @JSONField(serializeUsing = Aria2MethodSerializer.class)
    Aria2Method method;
    String id;
    String jsonrpc = "2.0";
    List<Object> params = new ArrayList<>();

    public void addParam(Object obj) {
        params.add(obj);
    }
    public void createUuid() {
        this.id = UUID.randomUUID().toString();
    }
}
