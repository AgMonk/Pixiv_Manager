package com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.res;

import com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.entity.PixivBookmarksAdd;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @author bx002
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PixivResBookmarksAdd extends PixivResponse<PixivBookmarksAdd> implements Serializable {
}
