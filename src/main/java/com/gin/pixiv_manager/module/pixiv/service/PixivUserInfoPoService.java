package com.gin.pixiv_manager.module.pixiv.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gin.pixiv_manager.module.pixiv.entity.PixivUserInfoPo;
import com.gin.pixiv_manager.sys.utils.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author bx002
 */
@Transactional(rollbackFor = Exception.class)
public interface PixivUserInfoPoService extends IService<PixivUserInfoPo> {
    /**
     * 保存&更新用户信息
     * @param userInfoPo 用户信息
     */
    default void saveUserInfo(PixivUserInfoPo userInfoPo) {
        saveUserInfo(Collections.singleton(userInfoPo));
    }

    /**
     * 保存&更新用户信息
     * @param userInfo 用户信息
     */
    default void saveUserInfo(Collection<PixivUserInfoPo> userInfo) {
        if (StringUtils.isEmpty(userInfo)) {
            return;
        }
        final List<Long> id = userInfo.stream()
                .map(PixivUserInfoPo::getUserId).distinct().collect(Collectors.toList());
        final List<Long> existsId = listByIds(id).stream()
                .map(PixivUserInfoPo::getUserId).distinct().collect(Collectors.toList());

        final List<PixivUserInfoPo> existUserInfo = userInfo.stream()
                .filter(i -> existsId.contains(i.getUserId())).collect(Collectors.toList());
        if (existUserInfo.size() > 0) {
            updateBatchById(existUserInfo);
        }
        final List<PixivUserInfoPo> newUserInfo = userInfo.stream()
                .filter(i -> !existsId.contains(i.getUserId())).collect(Collectors.toList());
        if (newUserInfo.size() > 0) {
            saveBatch(newUserInfo);
        }
    }
}