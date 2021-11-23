package com.gin.pixiv_manager.module.files.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gin.pixiv_manager.module.files.entity.Aria2DownloadTaskPo;
import org.apache.ibatis.annotations.CacheNamespace;
import org.springframework.stereotype.Repository;

/**
 * @author bx002
 */
@Repository
@CacheNamespace(flushInterval = 1L * 60 * 1000)
public interface Aria2DownloadTaskPoDao extends BaseMapper<Aria2DownloadTaskPo> {
}