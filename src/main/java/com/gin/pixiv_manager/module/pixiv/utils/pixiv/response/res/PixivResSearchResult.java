package com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.res;

import com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.body.PixivSearchResult;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author bx002
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PixivResSearchResult extends PixivResponse<PixivSearchResult> implements Serializable {
}
