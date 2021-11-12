package com.gin.pixiv_manager.module.pixiv.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gin.pixiv_manager.module.pixiv.entity.PixivIllustPo;
import com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.res.PixivResBookmarksAdd;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.Future;

/**
 * @author bx002
 */
@Transactional(rollbackFor = Exception.class)
public interface PixivIllustPoService extends IService<PixivIllustPo> {

    /**
     * 查询一个作品详情 如果数据库中有，且最后更新时间大于指定值 则直接使用数据库值
     * @param pid             pid
     * @param dataUpdatedTime 更新时间
     * @return 作品详情
     */
    Future<PixivIllustPo> findIllust(long pid, Long dataUpdatedTime);

    /**
     * 发送请求获取一个作品详情 并保存
     * @param pid pid
     * @return 作品详情
     */
    Future<?> findIllust(long pid);

    /**
     * 为作品添加表情
     * @param pid pid
     * @return 响应
     */
    Future<PixivResBookmarksAdd> addTag(long pid);
}