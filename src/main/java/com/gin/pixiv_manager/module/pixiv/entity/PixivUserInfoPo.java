package com.gin.pixiv_manager.module.pixiv.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.entity.PixivIllust;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsKey;
import com.gitee.sunchenbin.mybatis.actable.annotation.TableComment;
import com.sun.source.tree.CompilationUnitTree;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import com.baomidou.mybatisplus.annotation.TableId;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.UUID;

/**
 * Pixiv用户信息
 * @author bx002
 */
@Data
@TableName(value = "t_pixiv_entity_user_info", autoResultMap = true)
@TableComment("Pixiv用户信息")
@NoArgsConstructor
@AllArgsConstructor
public class PixivUserInfoPo implements Serializable {
    @TableId
    @IsKey
    @Column(comment = "uid")
    Long userId;

    @Column(comment = "名称", isNull = false)
    String userName;

    public PixivUserInfoPo(PixivIllust pixivIllust) {
        BeanUtils.copyProperties(pixivIllust,this);
    }
}