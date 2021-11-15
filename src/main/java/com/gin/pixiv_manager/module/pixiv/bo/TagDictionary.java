package com.gin.pixiv_manager.module.pixiv.bo;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gin.pixiv_manager.module.pixiv.entity.PixivTagPo;
import com.gin.pixiv_manager.module.pixiv.service.PixivTagPoService;
import com.gin.pixiv_manager.sys.utils.StringUtils;
import lombok.Data;
import org.nlpcn.commons.lang.jianfan.JianFan;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static com.gin.pixiv_manager.module.pixiv.entity.PixivTagPo.PATTERN_CHARACTER_IP;

/**
 * 根据Tag生成的字典  用于提供翻译建议
 * @author bx002
 */
@Data
public class TagDictionary implements Serializable {
    final TreeMap<String, String> believableDic = new TreeMap<>();
    final TreeMap<String, String> questionableDic = new TreeMap<>();

    List<PixivTagPo> completedTags;

    @JSONField(serialize = false)
    final PixivTagPoService service;

    public TagDictionary(PixivTagPoService service) {
        this.service = service;

        handleRedirectTags(believableDic);

        handleCompletedTags(believableDic);

        handleUnCompletedTags(questionableDic);
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
     * 比较两个字符串的中文占比 高作为翻译 低作为原文
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
     * 添加查询条件 查询已完成tag（自定义翻译有值的）
     * @param qw 条件
     */
    public static void selectCompleted(QueryWrapper<PixivTagPo> qw) {
        qw.isNotNull("custom_translation");
    }

    /**
     * 添加查询条件 查询未完成tag（自定义翻译和重定向均无值的）
     * @param qw 条件
     */
    public static void selectUnCompleted(QueryWrapper<PixivTagPo> qw) {
        qw.isNull("custom_translation").isNull("redirect");
    }

    /**
     * 添加查询条件 重定向tag
     * @param qw 条件
     */
    public static void selectRedirect(QueryWrapper<PixivTagPo> qw) {
        qw.isNotNull("redirect");
    }

    private static void putDic(Map<String, String> dic, String key, String value) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(value)) {
            return;
        }
        dic.put(key, value);
    }

    /**
     * 处理重定向tag
     * @param dic 字典
     */
    private void handleRedirectTags(Map<String, String> dic) {
        final QueryWrapper<PixivTagPo> qw = new QueryWrapper<>();
        selectRedirect(qw);
        final List<PixivTagPo> redirectTags = service.list(qw);
        final List<String> redirectTagName = redirectTags.stream().map(PixivTagPo::getRedirect).distinct().collect(Collectors.toList());
        HashMap<String, String> completedTagMap = new HashMap<>();
        service.listByIds(redirectTagName).forEach(t -> completedTagMap.put(t.getTag(), t.getCustomTranslation()));
        for (PixivTagPo redirectTag : redirectTags) {
            final String c = completedTagMap.get(redirectTag.getRedirect());
            putDic(dic, redirectTag.getTag(), c);
            putDic(dic, redirectTag.getOriginalTranslation(), c);
        }
    }

    /**
     * 解析关键字
     * @param keywords 关键字列表
     * @param tag      tag
     */
    private static void parseKeyword(List<String> keywords, String tag) {
        if (StringUtils.isEmpty(tag)) {
            return;
        }
        final Matcher matcher = PATTERN_CHARACTER_IP.matcher(tag);
        if (matcher.find()) {
            keywords.add(matcher.group(1).trim());
        } else {
            final double c1 = 1.0 * countChinese(tag) / tag.length();
            final int length = c1 > 0.5 ? 2 : 5;
            keywords.add(tag.substring(0, Math.min(tag.length(), length)));
            keywords.add(tag.substring(Math.max(0, tag.length() - length)));
        }
    }

    /**
     * 处理未完成tag
     * @param dic 字典
     */
    private void handleUnCompletedTags(Map<String, String> dic) {
        final QueryWrapper<PixivTagPo> qw = new QueryWrapper<>();
        qw.isNotNull("original_translation");
        selectUnCompleted(qw);
        service.list(qw).forEach(tag -> {
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

    private static boolean containsKeywords(List<String> keywords, PixivTagPo completedTag) {
        final String tag = completedTag.getTag().toLowerCase();
        final String cTag = completedTag.getCustomTranslation();
        String oTag = completedTag.getOriginalTranslation();
        oTag = StringUtils.isEmpty(oTag) ? null : oTag.toLowerCase();
        for (String keyword : keywords) {
            if (tag.contains(keyword.toLowerCase())) {
                return true;
            }
            if (cTag.contains(keyword.toLowerCase())) {
                return true;
            }
            if (oTag != null && oTag.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 处理已完成tag
     * @param dic 字典
     */
    private void handleCompletedTags(Map<String, String> dic) {
        final QueryWrapper<PixivTagPo> qw = new QueryWrapper<>();
        selectCompleted(qw);
        this.completedTags = service.list(qw);
        completedTags.forEach(tag -> {
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
     * 自定义翻译建议
     * @param tagPo tag
     * @return 自定义翻译建议
     */
    public List<String> suggestCustomTranslation(PixivTagPo tagPo) {
        final List<String> suggests = new ArrayList<>();

        handleTag(suggests, tagPo.getTag());
        handleTag(suggests, tagPo.getOriginalTranslation());

        return suggests.stream().distinct().collect(Collectors.toList());
    }

    private void handleTag(List<String> suggests, String tag) {
        if (StringUtils.isEmpty(tag)) {
            return;
        }
        handleTag(believableDic, suggests, tag);
        handleTag(questionableDic, suggests, tag);
    }

    private void handleTag(TreeMap<String, String> dic, List<String> suggests, String tag) {
//        词汇直接出现
        if (dic.containsKey(tag)) {
            suggests.add(dic.get(tag));
        }

//      原tag是 人物+IP 结构
        final Matcher matcher = PATTERN_CHARACTER_IP.matcher(tag);
        if (matcher.find()) {
            final String character = matcher.group(1).trim();
            final String ip = matcher.group(2).trim();
            final String tCharacter = dic.get(character);
            final String tIp = dic.get(ip);
            if (dic.containsKey(character) && dic.containsKey(ip)) {
                suggests.add(String.format("%s(%s)", tCharacter, tIp));
            } else if (dic.containsKey(character)) {
                suggests.add(String.format("%s(%s)", tCharacter, ip));
            } else if (dic.containsKey(ip)) {
                suggests.add(String.format("%s(%s)", character, tIp));
            }
        }

        /*todo*/
    }

    /**
     * 重定向建议
     */
    public List<PixivTagPo> suggestRedirect(PixivTagPo tagPo) {
//     解析关键字
        List<String> keywords = new ArrayList<>();
        parseKeyword(keywords, tagPo.getTag());
        parseKeyword(keywords, tagPo.getOriginalTranslation());

//        在已完成tag中查询关键字
        final List<PixivTagPo> suggests = new ArrayList<>();

        for (PixivTagPo completedTag : this.completedTags) {
            if (containsKeywords(keywords, completedTag)) {
                suggests.add(completedTag);
            }
        }

        return suggests.stream().distinct().collect(Collectors.toList());
    }
}
