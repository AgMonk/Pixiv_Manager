package com.gin.pixiv_manager.module.pixiv.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gin.pixiv_manager.module.pixiv.entity.PixivIllustPo;
import org.apache.ibatis.annotations.CacheNamespace;
import org.springframework.stereotype.Repository;

/**
 * @author bx002
 */
@Repository
@CacheNamespace(flushInterval = 5L * 60 * 1000)
public interface IllustPoDao extends BaseMapper<PixivIllustPo> {
}