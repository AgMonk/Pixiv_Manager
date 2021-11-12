package com.gin.pixiv_manager.module.pixiv.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gin.pixiv_manager.module.pixiv.entity.PixivIllustTagPo;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author bx002
 */
@Transactional(rollbackFor = Exception.class)
public interface PixivIllustTagPoService extends IService<PixivIllustTagPo> {
    /**
     * 根据pid查询tag
     * @param pid pid
     * @return 根据pid查询tag
     */
    default List<PixivIllustTagPo> listByPid(long pid){
        final QueryWrapper<PixivIllustTagPo> qw = new QueryWrapper<>();
        qw.eq("pid",pid);
        return list(qw);
    }

    /**
     * 根据pid查询tag名称
     * @param pid pid
     * @return 根据pid查询tag名称
     */
    default List<String> listTagByPid(long pid){
        return listByPid(pid).stream().map(PixivIllustTagPo::getTag).collect(Collectors.toList());
    }

    /**
     * 保存和更新作品的tag
     * @param pid  pid
     * @param tags tags
     */
    default void savePixivIllustTag(long pid, List<String> tags) {
        tags.removeAll(listTagByPid(pid));
        if (tags.size() == 0) {
            return;
        }
        saveBatch(PixivIllustTagPo.parse(pid, tags));
    }


}