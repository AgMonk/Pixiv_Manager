package com.gin.pixiv_manager.module.files.utils.response;

import lombok.Data;

import java.io.Serializable;

/**
 * @author bx002
 * @date 2021/2/3 16:17
 */
@Data
public class Aria2Uri implements Serializable {
    String uri;
    String status;
}
