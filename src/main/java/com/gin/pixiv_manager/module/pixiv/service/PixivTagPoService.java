package com.gin.pixiv_manager.module.pixiv.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gin.pixiv_manager.module.pixiv.entity.PixivTagPo;
import com.gin.pixiv_manager.sys.utils.StringUtils;
import org.nlpcn.commons.lang.jianfan.JianFan;
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

    private static void putDic(Map<String, String> dic, String key, String value) {
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
     * 单个字符是否为中文
     * @param c 字符
     * @return 单个字符是否为中文
     */
    static boolean isChinese(char c) {
        return c >= 0x4E00 && c <= 0x9FA5;
    }

    static int countChinese(String s) {
        int i = 0;
        for (char c : s.toCharArray()) {
            i += isChinese(c) ? 1 : 0;
        }
        return i;
    }

    /**
     * 比较两个字符串的中文占比 高的作为翻译 低的作为原文
     * @param dic 字典
     * @param s1  s1
     * @param s2  s2
     */
    static void compareChineseCount(Map<String, String> dic, String s1, String s2) {
        final double c1 = 1.0 * countChinese(s1) / s1.length();
        final double c2 = 1.0 * countChinese(s2) / s2.length();

//        如果中文占比均小于0.5 跳过
        if (c1 < 0.7 && c2 < 0.7) {
            return;
        }
//      有且仅有一个的中文占比高于0.7时
        if (c1 > c2) {
            putChineseWhenNotContain(dic, s2, s1, c2, c1, "低");
            return;
        }
        if (c1 < c2) {
            putChineseWhenNotContain(dic, s1, s2, c1, c2, "低");
            return;
        }
        if (c1 == 1.0 && c2 == 1.0) {
            putChineseWhenNotContain(dic, s1, s2, c1, c2, "全中");
        }

//      有且仅有一个的简化字与自身相同
        final String j1 = JianFan.f2j(s1);
        final String j2 = JianFan.f2j(s2);
        if (s1.equals(j1) && !s2.equals(j2)) {
            putChineseWhenNotContain(dic, s2, s1, c2, c1, "繁");
            return;
        }
        if (!s1.equals(j1) && s2.equals(j2)) {
            putChineseWhenNotContain(dic, s1, s2, c1, c2, "繁");
            return;
        }
        if (s1.equals(j1)) {
            putChineseWhenNotContain(dic, s1, s2, c1, c2, "同");
            return;
        }


        System.err.printf("[无法识别] %s -> %s\n", s1, s2);
    }

    static void putChineseWhenNotContain(Map<String, String> dic, String s1, String s2, double c1, double c2, String reason) {
        if (!dic.containsKey(s1)) {
            final String j2 = JianFan.f2j(s2);
            System.out.printf("%s(%s)(%s) -> %s(%s)\n", s1, c1, reason, j2, c2);
            dic.put(s1, j2);
        }
    }

    /**
     * 处理重定向tag
     * @param dic 字典
     */
    private void handleRedirectTags(Map<String, String> dic) {
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
    private void handleCompletedTags(Map<String, String> dic) {
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
    private void handleUnCompletedTags(Map<String, String> dic) {
        final QueryWrapper<PixivTagPo> qw = new QueryWrapper<>();
        qw.isNotNull("original_translation");
        selectUnCompleted(qw);
        list(qw).forEach(tag -> {
//            原生翻译貌似有用
            final Matcher oMatcher = PATTERN_CHARACTER_IP.matcher(tag.getOriginalTranslation());
            final Matcher tMatcher = PATTERN_CHARACTER_IP.matcher(tag.getTag());
            if (tMatcher.find() && oMatcher.find()) {
                final String tChar = tMatcher.group(1).trim();
                final String oChar = oMatcher.group(1).trim();
//              根据中文的数量决定那个是原文哪个是翻译
                compareChineseCount(dic, tChar, oChar);

                final String tIp = tMatcher.group(2).trim();
                final String oIp = oMatcher.group(2).trim();

                compareChineseCount(dic, tIp, oIp);
            } else if (!tMatcher.find() && !oMatcher.find()) {
                compareChineseCount(dic, tag.getTag(), tag.getOriginalTranslation());
            }
        });

    }
}