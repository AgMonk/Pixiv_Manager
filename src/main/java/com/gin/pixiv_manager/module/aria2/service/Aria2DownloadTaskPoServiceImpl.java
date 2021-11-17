package com.gin.pixiv_manager.module.aria2.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gin.pixiv_manager.module.aria2.dao.Aria2DownloadTaskPoDao;
import com.gin.pixiv_manager.module.aria2.entity.Aria2DownloadTaskPo;
import com.gin.pixiv_manager.module.pixiv.bo.TagAnalysisResult;
import com.gin.pixiv_manager.module.pixiv.entity.PixivTagPo;
import com.gin.pixiv_manager.module.pixiv.service.PixivIllustTagPoService;
import com.gin.pixiv_manager.module.pixiv.service.PixivTagPoService;
import com.gin.pixiv_manager.sys.config.TaskExecutePool;
import com.gin.pixiv_manager.sys.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author bx002
 */
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
//@RequiredArgsConstructor
public class Aria2DownloadTaskPoServiceImpl extends ServiceImpl<Aria2DownloadTaskPoDao, Aria2DownloadTaskPo> implements Aria2DownloadTaskPoService {
    @Value("${rootPath}")
    private final String rootPath;

    private final ThreadPoolTaskExecutor fileExecutor = TaskExecutePool.getExecutor("file", 1);
    private List<File> allFiles = new ArrayList<>();

    private final PixivIllustTagPoService pixivIllustTagPoService;
    private final PixivTagPoService pixivTagPoService;

    public Aria2DownloadTaskPoServiceImpl(@Value("${rootPath}") String rootPath,
                                          PixivIllustTagPoService pixivIllustTagPoService,
                                          PixivTagPoService pixivTagPoService) throws IOException {
        this.rootPath = rootPath;
        this.pixivIllustTagPoService = pixivIllustTagPoService;
        this.pixivTagPoService = pixivTagPoService;

        updateAllFileList();
//        fileExecutor.execute(()-> {
//            try {
//                updateAllFileList();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });
    }

    @Override
    public String getRootPath() {
        return rootPath;
    }

    @Override
    public List<File> getAllFiles(String prefix) {
        String path = (rootPath + prefix).replace("/", "\\");
        return allFiles.stream().filter(file -> file.getPath().startsWith(path)).collect(Collectors.toList());
    }

    @Scheduled(cron = "0 0/5 * * * ?")
    @Override
    public void updateAllFileList() throws IOException {
        this.allFiles = FileUtils.listAllFiles(rootPath);
    }

    @Override
    public TagAnalysisResult getTagAnalysisResultByPid(long pid) {
        final List<String> tagList = pixivIllustTagPoService.listTagByPid(pid);
        if (tagList.size() == 0) {
            return null;
        }
        final HashSet<PixivTagPo> tags = pixivTagPoService.listSimplified(tagList);
        final TagAnalysisResult result = new TagAnalysisResult(tags);
        result.setPid(pid);
        return result;
    }

}