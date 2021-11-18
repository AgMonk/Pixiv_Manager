package com.gin.pixiv_manager.module.aria2.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gin.pixiv_manager.module.aria2.dao.Aria2DownloadTaskPoDao;
import com.gin.pixiv_manager.module.aria2.entity.Aria2DownloadTaskPo;
import com.gin.pixiv_manager.module.pixiv.bo.TagAnalysisResult;
import com.gin.pixiv_manager.module.pixiv.entity.PixivIllustPo;
import com.gin.pixiv_manager.module.pixiv.entity.PixivTagPo;
import com.gin.pixiv_manager.module.pixiv.service.PixivIllustPoService;
import com.gin.pixiv_manager.module.pixiv.service.PixivIllustTagPoService;
import com.gin.pixiv_manager.module.pixiv.service.PixivTagPoService;
import com.gin.pixiv_manager.sys.config.TaskExecutePool;
import com.gin.pixiv_manager.sys.utils.FileUtils;
import com.gin.pixiv_manager.sys.utils.ImageUtils;
import com.gin.pixiv_manager.sys.utils.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
    private final PixivIllustPoService illustPoService;

    public Aria2DownloadTaskPoServiceImpl(@Value("${rootPath}") String rootPath,
                                          PixivIllustTagPoService pixivIllustTagPoService,
                                          PixivTagPoService pixivTagPoService, PixivIllustPoService illustPoService) throws IOException {
        this.rootPath = rootPath;
        this.pixivIllustTagPoService = pixivIllustTagPoService;
        this.pixivTagPoService = pixivTagPoService;
        this.illustPoService = illustPoService;


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
    public List<File> getAllFiles(String prefix) throws IOException {
        if (allFiles.size() == 0) {
            updateAllFileList();
        }
        String path = (rootPath + prefix).replace("/", "\\");
        return allFiles.stream().filter(file -> file.getPath().startsWith(path)).collect(Collectors.toList());
    }

    @Scheduled(cron = "0 0/5 * * * ?")
    @Override
    public void updateAllFileList() throws IOException {
        this.allFiles = FileUtils.listAllFiles(rootPath);
    }

    @Override
    @Scheduled(cron = "0/30 * * * * ?")
    public void reEntryPixiv() throws IOException {
        /*检查线程是否空闲*/
        if (fileExecutor.getActiveCount() != 0) {
            return;
        }
        /*获取目录文件*/
        final List<File> fileList = FileUtils.listAllFiles(rootPath + "/pixiv/重新录入");
        final Map<Long, List<File>> fileMap = PixivIllustPo.groupFileByPid(fileList);
        fileExecutor.execute(() -> fileMap.forEach((pid, files) -> {
            /*检查文件是否损坏 如果损坏则移动到指定文件夹*/
            if (files.stream().anyMatch(file1 -> !ImageUtils.verifyImage(file1))) {
                try {
                    FileUtils.move(files, rootPath + "/pixiv/损坏文件");
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            /*请求数据*/
            try {
                final Future<PixivIllustPo> future = illustPoService.findIllust(pid);
                final PixivIllustPo illust = future.get(1, TimeUnit.MINUTES);
                /*拿到数据*/
                String pixivPath = rootPath + "/pixiv/待归档/" + TimeUtils.DATE_FORMATTER.format(ZonedDateTime.now());
                files.forEach(file -> {
                    try {
                        FileUtils.move(file, new File(pixivPath + "/" + PixivIllustPo.parseOriginalName(file.getName())));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });

            } catch (InterruptedException | TimeoutException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                if (e.getMessage().contains("该作品已被删除")) {
                    try {
                        FileUtils.move(files, rootPath + "/pixiv/档案已删除");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }

        }));
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