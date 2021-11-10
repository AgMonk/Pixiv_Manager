package com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.res;

import com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.body.PixivIllustDetail;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author bx002
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PixivResIllustDetail extends PixivResponse<PixivIllustDetail> implements Serializable {
}
