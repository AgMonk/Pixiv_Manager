package com.gin.pixiv_manager.module.pixiv.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gin.pixiv_manager.module.pixiv.entity.PixivIllustOldPo;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author bx002
 */
@Transactional(rollbackFor = Exception.class)
public interface PixivIllustOldPoService extends IService<PixivIllustOldPo> {

}