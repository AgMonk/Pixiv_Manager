package com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.res;

import com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.entity.PixivUserInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author bx002
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PixivResUserInfo extends PixivResponse<PixivUserInfo> implements Serializable {
}
