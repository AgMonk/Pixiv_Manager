package com.gin.pixiv_manager.module.pixiv.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gin.pixiv_manager.module.pixiv.entity.PixivTagPo;
import com.gin.pixiv_manager.sys.utils.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
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
//            自动判断一部分tag的类型
            newTags.forEach(tag -> {
                final Matcher bmkCountMatcher = PixivTagPo.PATTERN_BMK_COUNT.matcher(tag.getTag());
                if (bmkCountMatcher.find()) {
                    tag.setType(PixivTagPo.TYPE_BMK_COUNT);
                }
                final Matcher charIpMatcher = PixivTagPo.PATTERN_CHARACTER_IP.matcher(tag.getTag());
                if (charIpMatcher.find()) {
                    tag.setType(PixivTagPo.TYPE_CHARACTER_IP);
                }
            });

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

    /**
     * 添加查询条件 查询已完成tag（自定义翻译有值的）
     * @param qw 条件
     */
    static void selectCompleted(QueryWrapper<PixivTagPo> qw) {
        qw.isNotNull("custom_translation");
    }

    /**
     * 添加查询条件 查询未完成tag（自定义翻译和重定向均无值的）
     * @param qw 条件
     */
    static void selectUnCompleted(QueryWrapper<PixivTagPo> qw) {
        qw.isNull("custom_translation").isNull("redirect");
    }

    /**
     * 添加查询条件 重定向tag
     * @param qw 条件
     */
    static void selectRedirect(QueryWrapper<PixivTagPo> qw) {
        qw.isNotNull("redirect");
    }

    /**
     * 将列表中出现的含有重定向字段的标签 执行重定向
     * @param list 列表
     */
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