package com.gin.pixiv_manager.module.pixiv.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gin.pixiv_manager.sys.params_validation.annotation.NotEmpty;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsKey;
import com.gitee.sunchenbin.mybatis.actable.annotation.TableComment;
import com.gitee.sunchenbin.mybatis.actable.annotation.Unique;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * PixivCookie
 * @author bx002
 */
@Data
@TableName(value = "t_pixiv_entity_cookie", autoResultMap = true)
@TableComment("PixivCookie")
@ApiModel("PixivCookie")
public class PixivCookie implements Serializable {
    @TableId
    @IsKey
    @Column(length = 36, isNull = false)
    @ApiModelProperty(value = "用户ID", required = true)
    Long uid;

    @Column(comment = "名称", length = 60, isNull = false)
    @ApiModelProperty(value = "名称", required = true)
    @Unique
    String name;

    @Column(comment = "cookie", length = 5000, isNull = false)
    @ApiModelProperty(value = "cookie", required = true)
    @JSONField(serialize = false)
    @NotEmpty("cookie")
    String cookie;

    @Column(comment = "token", length = 5000, isNull = false)
    @ApiModelProperty(value = "token", required = true)
    @JSONField(serialize = false)
    @NotEmpty("token")
    String token;


}