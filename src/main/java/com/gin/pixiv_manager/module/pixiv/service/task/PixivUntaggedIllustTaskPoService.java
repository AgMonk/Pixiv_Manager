package com.gin.pixiv_manager.module.pixiv.service.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gin.pixiv_manager.module.pixiv.entity.tasks.PixivUntaggedIllustTaskPo;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author bx002
 */
@Transactional(rollbackFor = Exception.class)
public interface PixivUntaggedIllustTaskPoService extends IService<PixivUntaggedIllustTaskPo> {

    /**
     * 请求收藏夹
     */
    void findBookmarks() throws IOException;

    default List<Long> listPid(int count, List<Long> except) {
        final QueryWrapper<PixivUntaggedIllustTaskPo> qw = new QueryWrapper<>();
        qw.last("limit " + count);
        if (except.size() > 0) {
            qw.notIn("pid", except);
        }
        qw.orderByDesc("pid");
        return list(qw).stream().map(PixivUntaggedIllustTaskPo::getPid).collect(Collectors.toList());
    }
}