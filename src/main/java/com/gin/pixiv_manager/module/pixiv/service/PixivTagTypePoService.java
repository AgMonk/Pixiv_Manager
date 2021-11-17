package com.gin.pixiv_manager.module.pixiv.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gin.pixiv_manager.module.pixiv.entity.PixivTagTypePo;
import com.gin.pixiv_manager.sys.exception.BusinessException;
import com.gin.pixiv_manager.sys.utils.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author bx002
 */
@Transactional(rollbackFor = Exception.class)
public interface PixivTagTypePoService extends IService<PixivTagTypePo> {

    /**
     * 根据名称查询
     * @param name 名称
     * @return PixivTagTypePoPo
     */
    default PixivTagTypePo getByName(String name) {
        return getOne(new QueryWrapper<PixivTagTypePo>().eq("name", name));
    }

    /**
     * 校验分类合法性
     * @param name 名称
     */
    default void validTypeName(String name) {
        if (StringUtils.isEmpty(name) || getByName(name) == null) {
            throw new BusinessException(4000, "不存在该分类：" + name);
        }
    }

    default List<PixivTagTypePo> listAll() {
        final QueryWrapper<PixivTagTypePo> qw = new QueryWrapper<>();
        qw.orderByDesc("order");
        return list(qw);
    }

    default List<String> listAllName() {
        return listAll().stream().map(PixivTagTypePo::getName).collect(Collectors.toList());
    }
}