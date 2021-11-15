package com.gin.pixiv_manager.sys.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;

/**
 * 分页查询参数
 * @author bx002
 */
@Getter
@Setter
@ApiModel("分页查询条件")
@Validated
public class PageParams<T extends IFilter> implements Serializable {
    @ApiModelProperty(value = "当前页", required = true, example = "1")
    int page = 1;
    @ApiModelProperty(value = "每页条数", required = true, example = "10")
    int size = 10;
    @ApiModelProperty("其他过滤条件")
    T filter;
}
