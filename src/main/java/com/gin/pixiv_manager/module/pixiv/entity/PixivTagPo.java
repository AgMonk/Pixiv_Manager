package com.gin.pixiv_manager.module.pixiv.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.entity.PixivTag;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsKey;
import com.gitee.sunchenbin.mybatis.actable.annotation.TableComment;
import com.gitee.sunchenbin.mybatis.actable.annotation.Unique;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

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
    public static final String TYPE_CHARACTER = "人物";
    public static final String TYPE_CHARACTER_IP = "人物+IP";
    public static final String TYPE_IP = "作品/IP";
    public static final String TYPE_ACTION = "动作";
    public static final String TYPE_CLOTHING = "服装";
    public static final String TYPE_CP = "CP";

    @TableId
    @IsKey
    @Column(comment = "标签名称")
    String tag;

    @Column(comment = "原翻译", length = 500)
    String originalTranslation;

    @Column(comment = "自定义翻译", length = 500)
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    @Unique
    String customTranslation;

    @Column(comment = "重定向到其他Tag", length = 500)
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    String redirect;

    @Column(comment = "标签类型")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    String type;

    public PixivTagPo(PixivTag pixivTag) {
        this.tag = pixivTag.getTag();
        this.originalTranslation = pixivTag.getOriginalTranslation();
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
}