package com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * @author bx002
 */
@Data
public class PixivBookmarksAdd implements Serializable {
    @JSONField(alternateNames = "last_bookmark_id")
    Long lastBookmarkId;
    @SuppressWarnings("SpellCheckingInspection")
    @JSONField(alternateNames = "stacc_status_id")
    Long staccStatusId;
}
