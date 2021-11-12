package com.gin.pixiv_manager.module.pixiv.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gin.pixiv_manager.module.pixiv.entity.tasks.PixivUntaggedIllustTaskPo;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

/**
 * @author bx002
 */
@Transactional(rollbackFor = Exception.class)
public interface PixivUntaggedIllustTaskPoService extends IService<PixivUntaggedIllustTaskPo> {

    /**
     * 请求收藏夹
     */
    void findBookmarks() throws IOException;
}