package com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author bx002
 */
@Data
public class PixivUserInfo implements Serializable {
    Long userId;
    String image;
    String imageBig;
    String name;
    Boolean isFollowed;
}
