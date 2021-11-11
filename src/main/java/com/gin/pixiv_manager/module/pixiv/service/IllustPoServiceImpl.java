package com.gin.pixiv_manager.module.pixiv.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gin.pixiv_manager.module.pixiv.dao.IllustPoDao;
import com.gin.pixiv_manager.module.pixiv.entity.*;
import com.gin.pixiv_manager.module.pixiv.utils.pixiv.request.PixivRequest;
import com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.body.PixivIllustDetail;
import com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.res.PixivResIllustDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * @author bx002
 */
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class IllustPoServiceImpl extends ServiceImpl<IllustPoDao, PixivIllustPo> implements IllustPoService {
    private final PixivCookieService pixivCookieService;
    private final ThreadPoolTaskExecutor illustExecutor;
    private final PixivUserInfoPoService pixivUserInfoPoService;
    private final PixivTagPoService pixivTagPoService;
    private final PixivIllustTagPoService pixivIllustTagPoService;

    @Override
    public Future<PixivIllustPo> findIllust(long pid, Long dataUpdatedTime) {
        final PixivIllustPo po = getById(pid);
        if (po != null && po.getDataUpdatedTime() > dataUpdatedTime) {
            return AsyncResult.forValue(po);
        }
        return findIllust(pid);
    }

    @Override
    public Future<PixivIllustPo> findIllust(long pid) {
        final PixivCookie pixivCookie = pixivCookieService.get();
        return illustExecutor.submit(() -> {
            PixivResIllustDetail res;
            for (int i = 0; i < 10; i++) {
                try {
                    res = PixivRequest.findIllustDetail(pixivCookie.getCookie(), pid);
                    final PixivIllustDetail body = res.getBody();
                    final PixivIllustPo po = new PixivIllustPo(body, res.getTimestamp());
                    saveOrUpdate(po);

                    handleUserInfo(body);
                    handleTags(body.getId(), body.getTags().getTags().stream().map(PixivTagPo::new).collect(Collectors.toList()));

                    return po;
                } catch (IOException e) {
                    Thread.sleep(5000);
                    e.printStackTrace();
                }
            }
            return null;
        });
    }

    /**
     * 处理标签
     * @param tags 标签
     */
    private void handleTags(Long pid, Collection<PixivTagPo> tags) {
//        保存tag
        pixivTagPoService.saveTags(tags);
//        保存作品的tag
        pixivIllustTagPoService.savePixivIllustTag(pid, tags.stream().map(PixivTagPo::getTag).collect(Collectors.toList()));
    }

    /**
     * 处理用户信息
     * @param body body
     */
    private void handleUserInfo(PixivIllustDetail body) {
        pixivUserInfoPoService.saveUserInfo(new PixivUserInfoPo(body));
    }
}