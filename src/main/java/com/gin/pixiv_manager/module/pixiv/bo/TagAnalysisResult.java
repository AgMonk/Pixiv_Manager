package com.gin.pixiv_manager.module.pixiv.bo;

import com.gin.pixiv_manager.module.pixiv.entity.PixivTagPo;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;

/**
 * 对tag类型进行分类
 * @author bx002
 */
@Data
public class TagAnalysisResult implements Serializable {
    HashSet<String> ip = new HashSet<>();
    HashSet<String> character = new HashSet<>();
    HashSet<String> other = new HashSet<>();

    public TagAnalysisResult(Collection<PixivTagPo> tags) {
        tags.forEach(tag -> {
            final String finalTranslation = tag.getFinalTranslation();
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
//                    替换括号
                    final String trans = finalTranslation.replace("（", "(").replace("）", ")");
                    final Matcher matcher = PixivTagPo.PATTERN_CHARACTER_IP.matcher(trans);
                    if (matcher.find()) {
                        character.add(matcher.group(1).trim());
                        ip.add(matcher.group(2).trim());
                    } else {
                        other.add(trans);
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
        return getSortedList(ip);
    }

    public List<String> getSortedChar() {
        return getSortedList(character);
    }

    public List<String> getSortedOther() {
        return getSortedList(other);
    }

    public List<String> getAll() {
        final ArrayList<String> list = new ArrayList<>();
        list.addAll(getSortedIp());
        list.addAll(getSortedChar());
        list.addAll(getSortedOther());
        return list;
    }
}
