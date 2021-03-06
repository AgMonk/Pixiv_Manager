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
import java.util.Collections;
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

                    handleTags(body.getId(), body.getTags().getTags().stream().map(PixivTagPo::new).collect(Collectors.toList()));
                    handleUserInfo(body);

                    return po;
                } catch (IOException e) {
                    if (e.getMessage().contains(MESSAGE_DELETED)) {

                        final PixivIllustOldPo oldData = pixivIllustOldPoService.getById(pid);
                        if (oldData == null) {
//                            ?????????????????? ????????????
                            throw e;
                        }
                        /* ??????????????? ???????????????*/
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
        final List<String> illustTagNames = pixivIllustTagPoService.listTagByPid(Collections.singleton(pid));
        if (illustTagNames.size() == 0) {
            throw new BusinessException(4000, "??????Tag??????????????????????????? pid = " + pid);
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
     * ????????????
     * @param tags ??????
     */
    private void handleTags(Long pid, Collection<PixivTagPo> tags) {
//        ??????tag
        pixivTagPoService.saveTags(tags);
//        ???????????????tag
        pixivIllustTagPoService.savePixivIllustTag(pid, tags.stream().map(PixivTagPo::getTag).collect(Collectors.toList()));
    }

    /**
     * ??????????????????
     * @param body body
     */
    private void handleUserInfo(PixivIllustDetail body) {
        pixivUserInfoPoService.saveUserInfo(new PixivUserInfoPo(body));
    }

    private void handleUserInfo(Long userId, String userName) {
        pixivUserInfoPoService.saveUserInfo(new PixivUserInfoPo(userId, userName));
    }


}