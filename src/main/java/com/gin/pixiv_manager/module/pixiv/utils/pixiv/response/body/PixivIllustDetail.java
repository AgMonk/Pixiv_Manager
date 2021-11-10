package com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.body;

import com.alibaba.fastjson.annotation.JSONField;
import com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.entity.PixivBookmarkData;
import com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.entity.PixivTags;
import com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.entity.PixivUrls;
import lombok.Data;

import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * @author bx002
 */
@Data
public class PixivIllustDetail implements Serializable {

    /**
     * 插画
     */
    public final static int ILLUST_TYPE_ILLUSTRATION = 0;
    /**
     * 漫画
     */
    public final static int ILLUST_TYPE_MANGA = 1;
    /**
     * 动图
     */
    public final static int ILLUST_TYPE_GIF = 2;

    Long id;
    Integer likeCount;
    Integer height;
    Integer width;
    Integer bookmarkCount;
    Integer viewCount;
    PixivTags tags;
    PixivBookmarkData bookmarkData;
    @JSONField(alternateNames = {"title","illustTitle"})
    String title;
    PixivUrls urls;
    Integer illustType;
    Integer pageCount;
    String userName;
    Long userId;

    ZonedDateTime createDate;
    ZonedDateTime uploadDate;

}
