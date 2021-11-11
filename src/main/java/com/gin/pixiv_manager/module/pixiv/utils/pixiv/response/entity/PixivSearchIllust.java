package com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.entity.PixivBookmarkData;
import com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.entity.PixivUrls;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * @author bx002
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PixivSearchIllust extends PixivIllust implements Serializable {
    ZonedDateTime updateDate;
    List<String> tags;
}
