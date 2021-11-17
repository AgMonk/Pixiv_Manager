package com.gin.pixiv_manager.module.pixiv.bo;

import com.gin.pixiv_manager.sys.params_validation.annotation.NotEmpty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 设置标签的自定义翻译或重定向
 * @author bx002
 */
@Data
@ApiModel("设置标签的自定义翻译或重定向")
public class PixivTagPo4Set implements Serializable {
    @ApiModelProperty(value = "待设置的标签名", required = true)
    @NotEmpty("待设置的标签名")
    String tag;
    @ApiModelProperty(value = "自定义翻译", required = true)
    @NotEmpty("自定义翻译")
    String translation;
    @ApiModelProperty(value = "类型", required = true)
    @NotEmpty("类型")
    String type;
}