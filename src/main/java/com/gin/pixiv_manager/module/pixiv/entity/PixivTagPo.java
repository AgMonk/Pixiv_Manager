package com.gin.pixiv_manager.module.pixiv.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.entity.PixivTag;
import com.gitee.sunchenbin.mybatis.actable.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Pixiv标签Po
 * @author bx002
 */
@Data
@TableName(value = "t_pixiv_entity_tag", autoResultMap = true)
@TableComment("Pixiv标签Po")
@NoArgsConstructor
@AllArgsConstructor
public class PixivTagPo implements Serializable {
    public static final Pattern PATTERN_CHARACTER_IP = Pattern.compile("^(.+)[(（](.+)[)）]$");
    public static final Pattern PATTERN_SKIN = Pattern.compile("^(.+)<(.+)>$");
    public static final Pattern PATTERN_BMK_COUNT = Pattern.compile("^(.+?)(\\d+)users入り$");
    public static final String TYPE_CHARACTER = "人物";
    public static final String TYPE_CHARACTER_IP = "人物+作品";
    public static final String TYPE_IP = "作品";
    public static final String TYPE_BMK_COUNT = "收藏数";
    public static final String TYPE_SKIN = "皮肤";
    public static final String TYPE_SERIES = "系列";
    public static final String USELESS_PREFIX = "#";


    @TableId
    @IsKey
    @Column(comment = "标签名称")
    String tag;

    @Column(comment = "原翻译", length = 200)
    String originalTranslation;

    @Column(comment = "自定义翻译", length = 100)
//    @TableField(updateStrategy = FieldStrategy.IGNORED)
    @Unique
    String customTranslation;

    @Column(comment = "重定向到其他Tag", length = 100)
//    @TableField(updateStrategy = FieldStrategy.IGNORED)
    @Index(columns = {"custom_translation", "redirect", "type", "count"})
    String redirect;

    @Column(comment = "标签类型", length = 50)
//    @TableField(updateStrategy = FieldStrategy.IGNORED)
    String type;

    @Column(comment = "被使用次数", defaultValue = "0")
    Integer count;

    @TableField(exist = false)
    List<String> suggest;

    @TableField(exist = false)
    List<PixivTagPo> suggestRedirect;

    @TableField(exist = false)
    List<String> examples;


    public PixivTagPo(PixivTag pixivTag) {
        this(pixivTag.getTag(), pixivTag.getOriginalTranslation());
    }

    public PixivTagPo(String tag, String originalTranslation) {
        this.tag = tag;
        if (this.tag.startsWith(USELESS_PREFIX)) {
            this.tag = this.tag.substring(1);
        }
        this.originalTranslation = originalTranslation;
        if (this.originalTranslation != null) {
            if (this.originalTranslation.startsWith(USELESS_PREFIX)) {
                this.originalTranslation = this.originalTranslation.substring(1);
            }
            this.originalTranslation = this.originalTranslation.replace("（", "(").replace("）", ")");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PixivTagPo that = (PixivTagPo) o;

        return getTag().equals(that.getTag());
    }

    @Override
    public int hashCode() {
        return getTag().hashCode();
    }

    public PixivTagPo copy() {
        PixivTagPo entity = new PixivTagPo();
        BeanUtils.copyProperties(this, entity);
        return entity;
    }
}