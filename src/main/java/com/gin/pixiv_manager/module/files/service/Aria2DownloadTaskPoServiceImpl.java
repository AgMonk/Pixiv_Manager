package com.gin.pixiv_manager.module.files.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gin.pixiv_manager.module.files.config.Aria2Config;
import com.gin.pixiv_manager.module.files.dao.Aria2DownloadTaskPoDao;
import com.gin.pixiv_manager.module.files.entity.Aria2DownloadTaskPo;
import com.gin.pixiv_manager.module.pixiv.bo.TagAnalysisResult;
import com.gin.pixiv_manager.module.pixiv.entity.PixivTagPo;
import com.gin.pixiv_manager.module.pixiv.service.PixivIllustPoService;
import com.gin.pixiv_manager.module.pixiv.service.PixivIllustTagPoService;
import com.gin.pixiv_manager.module.pixiv.service.PixivTagPoService;
import com.gin.pixiv_manager.sys.config.TaskExecutePool;
import com.gin.pixiv_manager.sys.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
public class Aria2DownloadTaskPoServiceImpl extends ServiceImpl<Aria2DownloadTaskPoDao, Aria2DownloadTaskPo> implements Aria2DownloadTaskPoService {
    private List<File> allFiles = new ArrayList<>();

    private final PixivIllustTagPoService pixivIllustTagPoService;
    private final PixivTagPoService pixivTagPoService;
    private final PixivIllustPoService illustPoService;
    private final Aria2Config aria2Config;
    private final ThreadPoolTaskExecutor fileExecutor = TaskExecutePool.getExecutor("file", 1);

//    public Aria2DownloadTaskPoServiceImpl() throws IOException {
//        fileExecutor.execute(() -> {
//            try {
//                updateAllFileList();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });
//    }

    @Override
    public Aria2Config getConfig() {
        return aria2Config;
    }

    @Override
    public List<File> getAllFiles(String prefix) throws IOException {
        if (allFiles.size() == 0) {
            fileExecutor.execute(() -> {
                try {
                    updateAllFileList();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            return new ArrayList<>();
        }
        String path = (getRootPath() + prefix).replace("/", "\\");
        return allFiles.stream().filter(file -> file.getPath().startsWith(path)).collect(Collectors.toList());
    }

    @Scheduled(cron = "0 0/5 * * * ?")
    @Override
    public void updateAllFileList() throws IOException {
        this.allFiles = FileUtils.listAllFiles(getRootPath());
    }

    private String getRootPath() {
        return getConfig().getRootPath();
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