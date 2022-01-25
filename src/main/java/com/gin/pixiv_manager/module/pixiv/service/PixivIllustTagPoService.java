package com.gin.pixiv_manager.module.pixiv.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gin.pixiv_manager.module.pixiv.bo.TagAnalysisResult;
import com.gin.pixiv_manager.module.pixiv.entity.PixivIllustTagPo;
import com.gin.pixiv_manager.sys.utils.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
    default List<PixivIllustTagPo> listByPid(Collection<Long> pid) {
        final QueryWrapper<PixivIllustTagPo> qw = new QueryWrapper<>();
        qw.in("pid", pid);
        return list(qw);
    }

    /**
     * 根据pid查询tag名称
     * @param pid pid
     * @return 根据pid查询tag名称
     */
    default List<String> listTagByPid(Collection<Long> pid) {
        return listByPid(pid).stream().map(PixivIllustTagPo::getTag).distinct().collect(Collectors.toList());
    }

    /**
     * 保存和更新作品的tag
     * @param pid  pid
     * @param tags tags
     */
    default void savePixivIllustTag(long pid, List<String> tags) {
        tags.removeAll(listTagByPid(Collections.singleton(pid)));
        if (tags.size() == 0) {
            return;
        }
        saveBatch(PixivIllustTagPo.parse(pid, tags));
    }

    default List<PixivIllustTagPo> listPidByTag(Collection<String> tag) {
        if (StringUtils.isEmpty(tag)) {
            return new ArrayList<>();
        }
        final QueryWrapper<PixivIllustTagPo> qw = new QueryWrapper<>();
        qw.in("tag", tag);
        return list(qw);
    }

    /**
     * 根据pid 获取标签分析结果
     * @param pid pid
     * @return 标签分析结果
     */
    TagAnalysisResult getTagAnalysisResultByPid(long pid);

    /**
     * 统计tag的使用次数
     * @return tag的使用次数
     */
    default List<PixivIllustTagPo> countTag() {
        final QueryWrapper<PixivIllustTagPo> qw = new QueryWrapper<>();
        qw.select("tag").eq("need_count", 1).groupBy("tag");
        final List<String> tags = list(qw).stream().map(PixivIllustTagPo::getTag).collect(Collectors.toList());
        if (tags.size() == 0) {
            return new ArrayList<>();
        }
        final QueryWrapper<PixivIllustTagPo> qw2 = new QueryWrapper<>();
        qw2.select("tag", "count(1) as count").in("tag", tags).groupBy("tag");

//        修改need_count
        final UpdateWrapper<PixivIllustTagPo> uw = new UpdateWrapper<>();
        uw.set("need_count", 0).in("tag", tags);
        update(uw);
        return list(qw2);
    }
}