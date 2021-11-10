package com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author bx002
 */
@Data
public class PixivTags implements Serializable {
    List<PixivTag> tags;
}
