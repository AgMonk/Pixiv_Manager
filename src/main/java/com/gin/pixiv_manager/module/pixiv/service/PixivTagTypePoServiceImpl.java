package com.gin.pixiv_manager.module.pixiv.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gin.pixiv_manager.module.pixiv.dao.PixivTagTypePoPoDao;
import com.gin.pixiv_manager.module.pixiv.entity.PixivTagTypePo;
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
public class PixivTagTypePoServiceImpl extends ServiceImpl<PixivTagTypePoPoDao, PixivTagTypePo> implements PixivTagTypePoService {
}