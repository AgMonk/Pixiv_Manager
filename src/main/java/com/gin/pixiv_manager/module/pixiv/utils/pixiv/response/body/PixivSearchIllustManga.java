package com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.body;

import com.alibaba.fastjson.annotation.JSONField;
import com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.entity.PixivSearchIllust;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author bx002
 */
@Data
public class PixivSearchIllustManga implements Serializable {
    Integer total;
    @JSONField(alternateNames = {"data","works"})
    List<PixivSearchIllust> data;
}
