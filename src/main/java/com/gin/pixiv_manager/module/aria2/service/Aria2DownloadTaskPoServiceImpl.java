package com.gin.pixiv_manager.module.aria2.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gin.pixiv_manager.module.aria2.dao.Aria2DownloadTaskPoDao;
import com.gin.pixiv_manager.module.aria2.entity.Aria2DownloadTaskPo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author bx002
 */
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class Aria2DownloadTaskPoServiceImpl extends ServiceImpl<Aria2DownloadTaskPoDao, Aria2DownloadTaskPo> implements Aria2DownloadTaskPoService {
    @Value("${rootPath}")
    String rootPath;

    @Override
    public String getRootPath() {
        return rootPath;
    }
}