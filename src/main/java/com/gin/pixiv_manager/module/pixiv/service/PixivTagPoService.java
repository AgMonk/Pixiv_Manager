package com.gin.pixiv_manager.module.pixiv.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gin.pixiv_manager.module.pixiv.entity.PixivTagPo;
import com.gin.pixiv_manager.sys.utils.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author bx002
 */
@Transactional(rollbackFor = Exception.class)
public interface PixivTagPoService extends IService<PixivTagPo> {
    /**
     * 保存tag
     * @param pixivTagPo tag
     */
    default void saveTags(PixivTagPo pixivTagPo) {
        saveTags(Collections.singleton(pixivTagPo));
    }

    /**
     * 保存tags
     * @param collection 集合
     */
    default void saveTags(Collection<PixivTagPo> collection) {
        if (StringUtils.isEmpty(collection)) {
            return;
        }
        final List<String> tags = collection.stream()
                .map(PixivTagPo::getTag).distinct().collect(Collectors.toList());
        final List<String> existsTags = listByIds(tags).stream()
                .map(PixivTagPo::getTag).distinct().collect(Collectors.toList());

        final List<PixivTagPo> newTags = collection.stream()
                .filter(i -> !existsTags.contains(i.getTag()))
                .collect(Collectors.toList());
        if (newTags.size() > 0) {
            saveBatch(newTags);
        }

//        final List<PixivTagPo> oldTags = collection.stream()
//                .filter(i -> existsTags.contains(i.getTag())).collect(Collectors.toList());
//        if (oldTags.size()>0) {
//            updateBatchById(oldTags);
//        }
    }

    /**
     * 精简查询tag（应用重定向）
     * @param tags tags
     * @return 精简查询tag（应用重定向）
     */
    default HashSet<PixivTagPo> listSimplified(Collection<String> tags) {
        final List<PixivTagPo> list = listByIds(tags);
        handleRedirect(list);
        return new HashSet<>(list);
    }

    private void handleRedirect(List<PixivTagPo> list) {
        final List<PixivTagPo> redirectTag = list.stream().filter(i -> i.getRedirect() != null).collect(Collectors.toList());
        if (redirectTag.size() > 0) {
            list.removeAll(redirectTag);
            final List<String> redirectTagName = redirectTag.stream().map(PixivTagPo::getRedirect).collect(Collectors.toList());
            list.addAll(listByIds(redirectTagName));
            handleRedirect(list);
        }
    }
}