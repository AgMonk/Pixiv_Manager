package com.gin.pixiv_manager.module.pixiv.bo;

import com.gin.pixiv_manager.module.pixiv.entity.PixivTagPo;
import com.gin.pixiv_manager.sys.utils.StringUtils;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;

/**
 * 对tags列表进行分析、分类
 * @author bx002
 */
@Data
public class TagAnalysisResult implements Serializable {
    Long pid;
    HashSet<String> ip = new HashSet<>();
    HashSet<String> skin = new HashSet<>();
    HashSet<String> character = new HashSet<>();
    HashSet<String> other = new HashSet<>();

    public TagAnalysisResult(Collection<PixivTagPo> tags) {
        tags.forEach(tag -> {
            final String finalTranslation =
                    !StringUtils.isEmpty(tag.getCustomTranslation()) ? tag.getCustomTranslation() : (
                            !StringUtils.isEmpty(tag.getOriginalTranslation()) ? tag.getOriginalTranslation() : tag.getTag()
                    );
            final String type = tag.getType();
            if (type == null) {
                other.add(finalTranslation);
                return;
            }
            //noinspection EnhancedSwitchMigration
            switch (type) {
                case PixivTagPo.TYPE_CHARACTER:
                    character.add(finalTranslation);
                    break;
                case PixivTagPo.TYPE_IP:
                    ip.add(finalTranslation);
                    break;
                case PixivTagPo.TYPE_CHARACTER_IP:
//                    tag为角色+ip，解析内容
                {
                    final Matcher matcher = PixivTagPo.PATTERN_CHARACTER_IP.matcher(finalTranslation);
                    if (matcher.find()) {
                        character.add(matcher.group(1).trim());
                        ip.add(matcher.group(2).trim());
                    } else {
                        other.add(finalTranslation);
                    }
                }
                break;
//                case PixivTagPo.TYPE_BMK_COUNT:
////                    tag为收藏数，解析内容
//                {
//                    final Matcher matcher = PixivTagPo.PATTERN_BMK_COUNT.matcher(finalTranslation);
//                    if (matcher.find()) {
//                        ip.add(matcher.group(1).trim());
//                    } else {
//                        other.add(finalTranslation);
//                    }
//                }
//                break;
                case PixivTagPo.TYPE_SKIN:
//                    tag为皮肤，解析内容
                {
                    final Matcher matcher = PixivTagPo.PATTERN_SKIN.matcher(finalTranslation);
                    if (matcher.find()) {
                        character.add(matcher.group(1).trim());
                        skin.add(matcher.group(2).trim());
                    } else {
                        other.add(finalTranslation);
                    }
                }
                break;
                default:
                    other.add(finalTranslation);
            }
        });
    }

    private static List<String> getSortedList(Collection<String> collection) {
        final ArrayList<String> list = new ArrayList<>(collection);
        list.sort(String::compareToIgnoreCase);
        return list;
    }

    public List<String> getSortedIp() {
        if (ip.size() == 0) {
            ip.add("原创");
        } else if (ip.contains("原创") && ip.size() > 1) {
            ip.remove("原创");
        }
        return getSortedList(ip);
    }

    public List<String> getSortedChar() {
        return getSortedList(character);
    }

    public List<String> getSortedOther() {
        return getSortedList(other);
    }

    public List<String> getSortedSkin() {
        return getSortedList(skin);
    }

    public List<String> getAll() {
        final ArrayList<String> list = new ArrayList<>();
        list.addAll(getSortedIp());
        list.addAll(getSortedChar());
        list.addAll(getSortedOther());
        return list;
    }
}
