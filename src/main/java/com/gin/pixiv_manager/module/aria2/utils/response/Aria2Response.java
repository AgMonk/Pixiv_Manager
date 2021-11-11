package com.gin.pixiv_manager.module.aria2.utils.response;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;

/**
 * 响应对象
 *
 * @author bx002
 * @date 2021/2/3 15:57
 */
@Data
public class Aria2Response implements Serializable {
    String id;
    String jsonrpc;
    HashMap<String, String> error;
}
