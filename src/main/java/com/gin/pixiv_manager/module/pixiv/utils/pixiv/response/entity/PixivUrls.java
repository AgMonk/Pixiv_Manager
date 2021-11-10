package com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author bx002
 */
@Data
public class PixivUrls implements Serializable {
    String small;
    String original;
    String mini;
    String thumb;
    String regular;
}
