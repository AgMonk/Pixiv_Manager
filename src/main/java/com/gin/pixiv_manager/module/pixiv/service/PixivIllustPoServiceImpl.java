package com.gin.pixiv_manager.module.pixiv.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gin.pixiv_manager.module.pixiv.bo.TagAnalysisResult;
import com.gin.pixiv_manager.module.pixiv.dao.IllustPoDao;
import com.gin.pixiv_manager.module.pixiv.entity.*;
import com.gin.pixiv_manager.module.pixiv.utils.pixiv.request.PixivRequest;
import com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.body.PixivIllustDetail;
import com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.res.PixivResBookmarksAdd;
import com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.res.PixivResIllustDetail;
import com.gin.pixiv_manager.sys.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static com.gin.pixiv_manager.module.files.service.PixivFilesServiceImpl.MESSAGE_DELETED;

/**
 * @author bx002
 */
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class PixivIllustPoServiceImpl extends ServiceImpl<IllustPoDao, PixivIllustPo> implements PixivIllustPoService {
    private final PixivCookieService pixivCookieService;
    private final ThreadPoolTaskExecutor illustExecutor;
    private final PixivUserInfoPoService pixivUserInfoPoService;
    private final PixivTagPoService pixivTagPoService;
    private final PixivIllustTagPoService pixivIllustTagPoService;
    private final ThreadPoolTaskExecutor bookmarkExecutor;
    private final PixivIllustOldPoService pixivIllustOldPoService;

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
                    if (e.getMessage().contains(MESSAGE_DELETED)) {

                        final PixivIllustOldPo oldData = pixivIllustOldPoService.getById(pid);
                        if (oldData == null) {
//                            没找到旧数据 抛出异常
                            throw e;
                        }
                        /* 找到旧数据 复制并返回*/
                        final PixivIllustPo po = oldData.toPixivIllustPo();
                        saveOrUpdate(po);
                        handleUserInfo(oldData.getUserId(), oldData.getUserName());
                        handleTags(pid, oldData.toPixivTagPoList());
                        return po;
                    } else {
                        Thread.sleep(5000);
                        e.printStackTrace();
                    }
                }
            }
            return null;
        });
    }

    @Override
    public Future<PixivResBookmarksAdd> addTag(long pid) {
        final List<String> illustTagNames = pixivIllustTagPoService.listTagByPid(pid);
        if (illustTagNames.size() == 0) {
            throw new BusinessException(4000, "没有Tag数据，请先请求详情");
        }
        final HashSet<PixivTagPo> pixivTagPos = pixivTagPoService.listSimplified(illustTagNames);
        final List<String> tags = new TagAnalysisResult(pixivTagPos).getAll();

        final PixivCookie pixivCookie = pixivCookieService.get();
        return bookmarkExecutor.submit(() -> {
            final PixivResBookmarksAdd res = PixivRequest.bookmarksAdd(pixivCookie.getCookie(), pixivCookie.getToken(), pid, tags);
            if (!res.getError() && res.getBody() != null && res.getBody().getLastBookmarkId() != null) {
                PixivIllustPo entity = new PixivIllustPo();
                entity.setId(pid);
                entity.setBookmarkId(res.getBody().getLastBookmarkId());
                updateById(entity);
            }
            return res;
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

    private void handleUserInfo(Long userId, String userName) {
        pixivUserInfoPoService.saveUserInfo(new PixivUserInfoPo(userId, userName));
    }


}