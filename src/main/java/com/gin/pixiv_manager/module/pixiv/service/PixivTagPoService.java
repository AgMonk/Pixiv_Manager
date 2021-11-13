package com.gin.pixiv_manager.module.pixiv.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gin.pixiv_manager.module.pixiv.entity.PixivTagPo;
import com.gin.pixiv_manager.sys.utils.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static com.gin.pixiv_manager.module.pixiv.entity.PixivTagPo.PATTERN_CHARACTER_IP;

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

    private static void putDic(TreeMap<String, String> dic, String key, String value) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(value)) {
            return;
        }
        dic.put(key, value);
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
                final Matcher charIpMatcher = PATTERN_CHARACTER_IP.matcher(tag.getTag());
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
     * 将列表中出现的含有重定向字段的标签 执行重定向
     * @param list 列表
     */
    private void handleRedirect(List<PixivTagPo> list) {
        final List<PixivTagPo> redirectTag = list.stream().filter(i -> i.getRedirect() != null).collect(Collectors.toList());
        if (redirectTag.size() > 0) {
            list.removeAll(redirectTag);
            final List<String> redirectTagName = redirectTag.stream().map(PixivTagPo::getRedirect).distinct().collect(Collectors.toList());
            list.addAll(listByIds(redirectTagName));
            handleRedirect(list);
        }
    }

    /**
     * 生成翻译字典
     * @return 字典
     */
    default Map<String, String> createDictionary() {
        final TreeMap<String, String> dic = new TreeMap<>();
        handleRedirectTags(dic);

        handleCompletedTags(dic);

        handleUnCompletedTags(dic);

        return dic;
    }

    /**
     * 处理重定向tag
     * @param dic 字典
     */
    private void handleRedirectTags(TreeMap<String, String> dic) {
        final QueryWrapper<PixivTagPo> qw = new QueryWrapper<>();
        selectRedirect(qw);
        final List<PixivTagPo> redirectTags = list(qw);
        final List<String> redirectTagName = redirectTags.stream().map(PixivTagPo::getRedirect).distinct().collect(Collectors.toList());
        HashMap<String, String> completedTagMap = new HashMap<>();
        listByIds(redirectTagName).forEach(t -> completedTagMap.put(t.getTag(), t.getCustomTranslation()));
        for (PixivTagPo redirectTag : redirectTags) {
            final String c = completedTagMap.get(redirectTag.getRedirect());
            putDic(dic, redirectTag.getTag(), c);
            putDic(dic, redirectTag.getOriginalTranslation(), c);
        }
    }

    /**
     * 处理已完成tag
     * @param dic 字典
     */
    private void handleCompletedTags(TreeMap<String, String> dic) {
        final QueryWrapper<PixivTagPo> qw = new QueryWrapper<>();
        selectCompleted(qw);
        final List<PixivTagPo> list = list(qw);
        list.forEach(tag -> {
            putDic(dic, tag.getTag(), tag.getCustomTranslation());
            putDic(dic, tag.getOriginalTranslation(), tag.getCustomTranslation());

//          如果是 人物+IP tag 额外处理 解析拆分保存
            final Matcher cMatcher = PATTERN_CHARACTER_IP.matcher(tag.getCustomTranslation());
            final Matcher tMatcher = PATTERN_CHARACTER_IP.matcher(tag.getTag());
            if (cMatcher.find()) {
                final String cChar = cMatcher.group(1).trim();
                final String cIp = cMatcher.group(2).trim();
                if (tMatcher.find()) {
                    final String tChar = tMatcher.group(1).trim();
                    final String tIp = tMatcher.group(2).trim();
                    putDic(dic, tChar, cChar);
                    putDic(dic, tIp, cIp);
                }
                if (tag.getOriginalTranslation() != null) {
                    final Matcher oMatcher = PATTERN_CHARACTER_IP.matcher(tag.getOriginalTranslation());
                    if (oMatcher.find()) {
                        final String oChar = oMatcher.group(1).trim();
                        final String oIp = oMatcher.group(2).trim();
                        putDic(dic, oChar, cChar);
                        putDic(dic, oIp, cIp);
                    }
                }

            }

        });
    }

    /**
     * 处理未完成tag
     * @param dic 字典
     */
    private void handleUnCompletedTags(TreeMap<String, String> dic) {
        final QueryWrapper<PixivTagPo> qw = new QueryWrapper<>();
        qw.isNotNull("original_translation");
        selectUnCompleted(qw);
        list(qw);
    }
}