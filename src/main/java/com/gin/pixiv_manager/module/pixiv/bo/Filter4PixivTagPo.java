package com.gin.pixiv_manager.module.pixiv.bo;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gin.pixiv_manager.module.pixiv.entity.PixivTagPo;
import com.gin.pixiv_manager.sys.params_validation.annotation.EffectiveValues;
import com.gin.pixiv_manager.sys.request.IFilter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author bx002
 */
@Data
@ApiModel("标签查询过滤条件")
public class Filter4PixivTagPo implements Serializable, IFilter {
    public static final String TYPE_1 = "未完成";
    public static final String TYPE_2 = "已完成";
    public static final String TYPE_3 = "重定向";
    @EffectiveValues(values = {TYPE_1, TYPE_2, TYPE_3})
    @ApiModelProperty(required = true, allowableValues = TYPE_1 + "," + TYPE_2 + "," + TYPE_3 + ",", example = TYPE_1)
    String type;

    @ApiModelProperty(value = "指定目录")
    String dirName;

    @Override
    public void handleQueryWrapper(QueryWrapper<?> queryWrapper) {
        @SuppressWarnings("unchecked") QueryWrapper<PixivTagPo> qw = (QueryWrapper<PixivTagPo>) queryWrapper;
        //noinspection EnhancedSwitchMigration
        switch (type) {
            case TYPE_1:
                TagDictionary.selectUnCompleted(qw);
                qw.orderByDesc("count");
                break;
            case TYPE_2:
                TagDictionary.selectCompleted(qw);
                break;
            case TYPE_3:
                TagDictionary.selectRedirect(qw);
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
    }
}