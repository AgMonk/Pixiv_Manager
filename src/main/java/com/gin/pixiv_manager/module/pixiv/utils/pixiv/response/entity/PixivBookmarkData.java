package com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * 收藏数据
 * @author bx002
 */
@Data
public class PixivBookmarkData implements Serializable {
    @JSONField(alternateNames = "private",name="private")
    Boolean isPrivate;
    Long id;
}
