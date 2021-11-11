package com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.body;

import com.alibaba.fastjson.annotation.JSONField;
import com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.entity.PixivBookmarkData;
import com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.entity.PixivIllust;
import com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.entity.PixivTags;
import com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.entity.PixivUrls;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * @author bx002
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PixivIllustDetail extends PixivIllust implements Serializable {
    ZonedDateTime uploadDate;
    PixivTags tags;
}
