package com.gin.pixiv_manager.module.pixiv.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gin.pixiv_manager.module.pixiv.entity.PixivTagPo;
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
    default void saveTags(Collection<PixivTagPo> collection){
        if (StringUtils.isEmpty(collection)) {
            return;
        }
        final List<String> tags = collection.stream()
                .map(PixivTagPo::getTag).distinct().collect(Collectors.toList());
        final List<String> existsTags = listByIds(tags).stream()
                .map(PixivTagPo::getTag).distinct().collect(Collectors.toList());

        final List<PixivTagPo> newTags = collection.stream()
                .filter(i -> !existsTags.contains(i.getTag())).collect(Collectors.toList());
        if (newTags.size()>0) {
            saveBatch(newTags);
        }
    }
}