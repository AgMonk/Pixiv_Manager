package com.gin.pixiv_manager.module.pixiv.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gin.pixiv_manager.module.pixiv.dao.PixivIllustTagPoDao;
import com.gin.pixiv_manager.module.pixiv.entity.PixivIllustTagPo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author bx002
 */
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class PixivIllustTagPoServiceImpl extends ServiceImpl<PixivIllustTagPoDao, PixivIllustTagPo> implements PixivIllustTagPoService {

}