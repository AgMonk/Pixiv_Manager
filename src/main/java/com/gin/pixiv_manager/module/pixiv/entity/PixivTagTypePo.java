package com.gin.pixiv_manager.module.pixiv.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsKey;
import com.gitee.sunchenbin.mybatis.actable.annotation.TableComment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Pixiv标签类型Po
 * @author bx002
 */
@Data
@TableName(value = "t_pixiv_entity_tag_type", autoResultMap = true)
@TableComment("Pixiv标签类型Po")
@NoArgsConstructor
@AllArgsConstructor
public class PixivTagTypePo implements Serializable {
    @TableId(type = IdType.AUTO)
    @IsKey
    @Column(isNull = false, isAutoIncrement = true)
    Long id;

    @Column(comment = "名称", length = 60, isNull = false)
    String name;

    @Column(comment = "排序", isNull = false, defaultValue = "50")
    Integer orderNo;

    public PixivTagTypePo(String name) {
        this.name = name;
    }
}