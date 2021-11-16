package com.gin.pixiv_manager.module.pixiv.bo;

import com.gin.pixiv_manager.sys.params_validation.annotation.EffectiveValues;
import com.gin.pixiv_manager.sys.params_validation.annotation.NotEmpty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

import static com.gin.pixiv_manager.module.pixiv.entity.PixivTagPo.*;

/**
 * 设置标签的自定义翻译或重定向
 * @author bx002
 */
@Data
@ApiModel("设置标签的自定义翻译或重定向")
public class PixivTagPo4Set implements Serializable {
    @NotEmpty("待设置的标签名")
    @ApiModelProperty(value = "待设置的标签名", required = true)
    String tag;
    @ApiModelProperty(value = "自定义翻译", required = true)
    @NotEmpty("translation")
    String translation;
    @ApiModelProperty(value = "类型", required = true)
    @EffectiveValues(prefix = "类型", values = {TYPE_CHARACTER
            , TYPE_CHARACTER_IP
            , TYPE_IP
            , TYPE_ACTION
            , TYPE_CLOTHING
            , TYPE_ITEM
            , TYPE_CP
            , TYPE_OTHER
            , TYPE_BMK_COUNT
            , TYPE_POSITION
            , TYPE_HAIRSTYLE
    })
    String type;
}