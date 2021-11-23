package com.gin.pixiv_manager.module.files.service;

import com.gin.pixiv_manager.module.files.config.Aria2Config;
import com.gin.pixiv_manager.module.pixiv.bo.TagAnalysisResult;
import com.gin.pixiv_manager.module.pixiv.entity.PixivIllustPo;
import com.gin.pixiv_manager.module.pixiv.service.PixivIllustPoService;
import com.gin.pixiv_manager.sys.config.TaskExecutePool;
import com.gin.pixiv_manager.sys.utils.FileUtils;
import com.gin.pixiv_manager.sys.utils.ImageUtils;
import com.gin.pixiv_manager.sys.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author bx002
 */
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class PixivFilesServiceImpl implements PixivFilesService {
    private final PixivIllustPoService illustPoService;
    private final Aria2Config aria2Config;
    private final ThreadPoolTaskExecutor fileExecutor = TaskExecutePool.getExecutor("pixiv-file", 1);
    private final Aria2DownloadTaskPoService aria2DownloadTaskPoService;

    private String getRootPath() {
        return aria2Config.getRootPath();
    }

    @Override
    @Scheduled(cron = "0/30 * * * * ?")
    public void reEntryPixiv() throws IOException {
        /*检查线程是否空闲*/
        if (fileExecutor.getActiveCount() != 0) {
            return;
        }
        /*获取目录文件*/
        final List<File> fileList = FileUtils.listAllFiles(getRootPath() + "/pixiv/重新录入");
        final Map<Long, List<File>> fileMap = PixivIllustPo.groupFileByPid(fileList);
        fileExecutor.execute(() -> fileMap.forEach((pid, files) -> {
            /*检查文件是否损坏 如果损坏则移动到指定文件夹*/
            if (files.stream().anyMatch(f -> !f.getName().endsWith("zip") && !ImageUtils.verifyImage(f))) {
                try {
                    FileUtils.move(files, getRootPath() + "/pixiv/损坏文件");
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            /*请求数据*/
            try {
                final Future<PixivIllustPo> future = illustPoService.findIllust(pid, ZonedDateTime.now().minusDays(1).toEpochSecond());
                final PixivIllustPo illust = future.get(1, TimeUnit.MINUTES);
                /*拿到数据*/
                String pixivPath = getRootPath() + "/pixiv/待归档/" + TimeUtils.DATE_FORMATTER.format(ZonedDateTime.now());
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
                        FileUtils.move(files, getRootPath() + "/pixiv/档案已删除");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }

        }));
    }

    @Override
    public void arrangeFiles(String dirName) throws IOException {
        final List<File> allFiles = aria2DownloadTaskPoService.getAllFiles("/pixiv/待归档/" + dirName);

        final Map<Long, List<File>> filesMap = PixivIllustPo.groupFileByPid(allFiles);
        if (filesMap.size() == 0) {
            return;
        }
        filesMap.forEach((pid, files) -> {
            final TagAnalysisResult result = aria2DownloadTaskPoService.getTagAnalysisResultByPid(pid);
            final List<String> ip = result.getSortedIp();
            if (ip.size() == 0) {
                ip.add("原创");
            }
            String destDirPath = getRootPath() + FileUtils.deleteIllegalChar(String.format("/pixiv/已归档/%s/%s/"
                    , String.join(",", ip)
                    , String.join(",", result.getSortedChar())

            ));
            files.forEach(file -> {
                try {
                    FileUtils.move(file, new File(destDirPath + FileUtils.deleteIllegalChar(file.getName())));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        });
    }
}