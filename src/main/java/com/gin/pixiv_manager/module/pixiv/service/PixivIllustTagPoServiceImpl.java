package com.gin.pixiv_manager.module.pixiv.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gin.pixiv_manager.module.pixiv.bo.TagAnalysisResult;
import com.gin.pixiv_manager.module.pixiv.dao.PixivIllustTagPoDao;
import com.gin.pixiv_manager.module.pixiv.entity.PixivIllustTagPo;
import com.gin.pixiv_manager.module.pixiv.entity.PixivTagPo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author bx002
 */
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class PixivIllustTagPoServiceImpl extends ServiceImpl<PixivIllustTagPoDao, PixivIllustTagPo> implements PixivIllustTagPoService {

    private final PixivTagPoService pixivTagPoService;


    @Override
    public TagAnalysisResult getTagAnalysisResultByPid(long pid) {
        final List<String> tagList = listTagByPid(Collections.singleton(pid));
        if (tagList.size() == 0) {
            return null;
        }
        final HashSet<PixivTagPo> tags = pixivTagPoService.listSimplified(tagList);
        final TagAnalysisResult result = new TagAnalysisResult(tags);
        result.setPid(pid);
        return result;
    }

    /**
     * 更新tag的使用次数
     */
//    @PostConstruct
    @Scheduled(cron = "10 0/10 * * * ?")
    public void updateTagCount() {
        final List<PixivTagPo> list = countTag().stream().map(PixivIllustTagPo::toPixivTagPo).collect(Collectors.toList());
        if (list.size() == 0) {
            log.info("tag使用次数未更新");
            return;
        }
        log.info("更新tag的使用次数 开始");
        pixivTagPoService.updateBatchById(list);
        log.info("更新tag的使用次数 完毕");
    }
}