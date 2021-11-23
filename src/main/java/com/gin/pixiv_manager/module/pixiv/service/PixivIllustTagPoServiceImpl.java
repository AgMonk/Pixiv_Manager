package com.gin.pixiv_manager.module.pixiv.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gin.pixiv_manager.module.pixiv.bo.TagAnalysisResult;
import com.gin.pixiv_manager.module.pixiv.dao.PixivIllustTagPoDao;
import com.gin.pixiv_manager.module.pixiv.entity.PixivIllustTagPo;
import com.gin.pixiv_manager.module.pixiv.entity.PixivTagPo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

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
        final List<String> tagList = listTagByPid(pid);
        if (tagList.size() == 0) {
            return null;
        }
        final HashSet<PixivTagPo> tags = pixivTagPoService.listSimplified(tagList);
        final TagAnalysisResult result = new TagAnalysisResult(tags);
        result.setPid(pid);
        return result;
    }
}