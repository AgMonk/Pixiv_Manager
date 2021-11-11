package com.gin.pixiv_manager.module.pixiv.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.entity.PixivTag;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsKey;
import com.gitee.sunchenbin.mybatis.actable.annotation.TableComment;
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
    public static final String TYPE_IP = "作品/IP";
    public static final String TYPE_ACTION = "动作";
    public static final String TYPE_CLOTHING = "服装";
    public static final String TYPE_CP = "CP";

    @TableId
    @IsKey
    @Column(comment = "标签名称")
    String tag;

    @Column(comment = "原翻译",length = 1000)
    String originalTranslation;

    @Column(comment = "自定义翻译",length = 1000)
    String customTranslation;

    @Column(comment = "重定向到其他Tag",length = 1000)
    String redirect;

    @Column(comment = "标签类型")
    String type;

    public PixivTagPo(PixivTag pixivTag) {
        this.tag = pixivTag.getTag();
        this.originalTranslation = pixivTag.getOriginalTranslation();
    }
}