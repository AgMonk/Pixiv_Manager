package com.gin.pixiv_manager.module.aria2.utils.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 下载参数
 *
 * @author bx002
 * @date 2021/2/3 16:45
 */
@Data
@Accessors(chain = true)
public class Aria2UriOption implements Serializable {
    String dir;
    @JSONField(name = "out")
    String fileName;
    String referer = "*";
    @JSONField(name = "https-proxy")
    String httpsProxy;
    StringBuilder header = new StringBuilder();

    public Aria2UriOption addHeader(String key, String value) {
        header.append(key).append(":").append(value).append("\\n");
        return this;
    }

    public Aria2UriOption addCookie(String cookie) {
        return addHeader("Cookie", cookie);
    }

}
