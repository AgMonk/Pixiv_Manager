package com.gin.pixiv_manager.sys.request;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * 作为过滤条件的接口
 * @author bx002
 */
public interface IFilter {
    /**
     * 向QueryWrapper中添加过滤条件
     * @param queryWrapper 查询条件
     */
    void handleQueryWrapper(QueryWrapper<?> queryWrapper);
}
