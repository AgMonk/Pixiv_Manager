package com.gin.pixiv_manager.module.files.service;

import com.gin.pixiv_manager.module.files.config.FilesConfig;
import com.gin.pixiv_manager.module.files.config.child.PixivConfig;
import com.gin.pixiv_manager.module.files.entity.Aria2DownloadTaskPo;
import com.gin.pixiv_manager.module.pixiv.bo.TagAnalysisResult;
import com.gin.pixiv_manager.module.pixiv.entity.PixivIllustPo;
import com.gin.pixiv_manager.module.pixiv.service.PixivIllustPoService;
import com.gin.pixiv_manager.module.pixiv.service.PixivIllustTagPoService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static com.gin.pixiv_manager.module.pixiv.entity.PixivIllustPo.ILLUST_TYPE_GIF;

/**
 * @author bx002
 */
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class PixivFilesServiceImpl implements PixivFilesService {
    private final static String PIXIV_RE_DOMAIN = "https://pixiv.re/";
    private final static String PIXIV_RE_DOMAIN_2 = "i.pixiv.re";
    public static final String MESSAGE_DELETED = "该作品已被删除";

    private final PixivIllustPoService illustPoService;
    private final FilesConfig filesConfig;
    private final ThreadPoolTaskExecutor fileExecutor = TaskExecutePool.getExecutor("pixiv-file", 1);
    private final Aria2DownloadTaskPoService aria2DownloadTaskPoService;
    private final PixivIllustTagPoService pixivIllustTagPoService;

    private String getRootPath() {
        return filesConfig.getRootPath();
    }

    private PixivConfig getPixivConfig() {
        return filesConfig.getPixiv();
    }

    @Override
    @Scheduled(cron = "0/30 * * * * ?")
    public void reEntryPixiv() throws IOException {
        /*检查线程是否空闲*/
        if (fileExecutor.getActiveCount() != 0) {
            return;
        }
        /*获取目录文件*/
        String reEntryPath = String.format("%s/%s/%s"
                , getRootPath()
                , getPixivConfig().getRootPath()
                , getPixivConfig().getReEntryDir()
        );
        final List<File> fileList = FileUtils.listAllFiles(reEntryPath);
        final Map<Long, List<File>> fileMap = PixivIllustPo.groupFileByPid(fileList);
        final List<Long> pidList = fileMap.keySet().stream().limit(3).collect(Collectors.toList());
        fileExecutor.execute(() ->
                pidList.forEach(pid ->
//                fileMap.forEach((pid, files) ->
                {
                    final List<File> files = fileMap.get(pid);
                    /*检查文件是否损坏 */
                    if (files.stream().anyMatch(f -> !f.getName().endsWith("zip") && !ImageUtils.verifyImage(f))) {
                        handleErrorFiles(pid, files);
                        return;
                    }
                    /*请求数据*/
                    Future<PixivIllustPo> future = illustPoService.findIllust(pid, ZonedDateTime.now().minusDays(1).toEpochSecond());
                    try {
                        final PixivIllustPo illust = future.get(1, TimeUnit.MINUTES);
                        /*拿到数据*/
                        String downloadPath = String.format("%s/%s/%s/%s/"
                                , getRootPath()
                                , getPixivConfig().getRootPath()
                                , getPixivConfig().getUntaggedDir()
                                , TimeUtils.DATE_FORMATTER.format(ZonedDateTime.now())
                        );
                        files.forEach(file -> {
                            File dest = new File(downloadPath + PixivIllustPo.parseOriginalName(file.getName()));
                            while (dest.exists()) {
                                if (file.length() == dest.length()) {
                                    FileUtils.deleteFile(file);
                                    return;
                                }
                                final String path = dest.getPath();
                                final int index = path.lastIndexOf(".");
                                final String start = path.substring(0, index);
                                final String end = path.substring(index);
                                final String newPath = start + "_bak" + end;
                                dest = new File(newPath);
                            }
                            try {
                                FileUtils.move(file, dest);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });

                    } catch (InterruptedException | TimeoutException e) {
                        future.cancel(true);
//                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        future.cancel(true);
                        if (e.getMessage().contains(MESSAGE_DELETED)) {
                            handleDeletedFiles(pid, files);
                        }
                    }

                }));
    }

    /**
     * 处理已经被删除的作品文件
     * @param files 文件
     */
    private void handleDeletedFiles(Long pid, List<File> files) {
        /*todo 尝试从旧数据中查询*/

        try {
            FileUtils.move(files, getDeletedDir());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 处理损坏的图片文件
     * @param pid   pid
     * @param files 文件
     */
    private void handleErrorFiles(long pid, List<File> files) {
        Future<PixivIllustPo> future = illustPoService.findIllust(pid, ZonedDateTime.now().minusDays(1).toEpochSecond());
        try {
            final PixivIllustPo illust = future.get(1, TimeUnit.MINUTES);
//            作品还在，重新下载，删除损坏文件
            if (illust != null) {
                if (downloadFile(illust) > 0) {
                    files.forEach(FileUtils::deleteFile);
                } else {
                    handleDeletedFiles(pid, files);
                }
            }
        } catch (ExecutionException e) {
//            作品已删除，移动到已删除文件夹
            if (e.getMessage().contains(MESSAGE_DELETED)) {
                handleDeletedFiles(pid, files);
            }
            e.printStackTrace();
        } catch (InterruptedException | TimeoutException e) {
            future.cancel(true);
            e.printStackTrace();
        }
    }

    private String getDeletedDir() {
        return String.format("%s/%s/%s"
                , getRootPath()
                , getPixivConfig().getRootPath()
                , getPixivConfig().getDeletedDir()
        );
    }

    @Override
    public void arrangeFiles(String dirName) throws IOException {
        final List<File> allFiles = aria2DownloadTaskPoService.getAllFiles("/pixiv/待归档/" + dirName);

        final Map<Long, List<File>> filesMap = PixivIllustPo.groupFileByPid(allFiles);
        if (filesMap.size() == 0) {
            return;
        }
        filesMap.forEach((pid, files) -> {
            final TagAnalysisResult result = pixivIllustTagPoService.getTagAnalysisResultByPid(pid);
            final List<String> ip = result.getSortedIp();
            if (ip.size() == 0) {
                ip.add("原创");
            }
            String destDirPath = getRootPath() + FileUtils.deleteIllegalChar(
                    String.format("/%s/%s/%s/%s/"
                            , getPixivConfig().getRootPath()
                            , getPixivConfig().getTaggedDir()
                            , String.join(",", ip)
                            , String.join(",", result.getSortedChar()))
            );
            files.forEach(file -> {
                try {
                    FileUtils.move(file, new File(destDirPath + FileUtils.deleteIllegalChar(file.getName())));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        });
    }

    /**
     * 下载一个Pixiv作品
     * @param illust 作品详情
     */
    @Override
    public int downloadFile(PixivIllustPo illust) {
        String downloadPath = String.format("%s/%s/%s/%s"
                , getRootPath()
                , getPixivConfig().getRootPath()
                , getPixivConfig().getUntaggedDir()
                , TimeUtils.DATE_FORMATTER.format(ZonedDateTime.now())
        );


        //                动图 添加一个任务
        if (ILLUST_TYPE_GIF.equals(illust.getType())) {
            String uuid = illust.getId() + "_u0";
            if (aria2DownloadTaskPoService.getById(uuid) != null) {
                log.warn("已经有相同任务 pid = {}", illust.getId());
                return 0;
            }
            final String oUrl = illust.getOriginalUrl();
            final String rUrl2 = oUrl.replace("i.pximg.net", PIXIV_RE_DOMAIN_2);
            String filename = oUrl.substring(oUrl.lastIndexOf("/") + 1);
            Aria2DownloadTaskPo task = new Aria2DownloadTaskPo();
            task.setDir(downloadPath);
            task.setFileName(filename);
            task.setUrls(List.of(oUrl, rUrl2));
            task.setUuid(uuid);
            task.setType("pixiv-gif");
            task.setPriority(2);
            task.setTimestamp(ZonedDateTime.now().toEpochSecond());
            aria2DownloadTaskPoService.save(task);
            log.info("添加 1 个 动图任务 {}", illust.getId());
            return 1;
        } else {
            //                其他 可能添加多个任务
            List<Aria2DownloadTaskPo> taskList = new ArrayList<>();
            for (int i = 0; i < illust.getPageCount(); i++) {
                final String uuid = illust.getId() + "_p" + i;
                if (aria2DownloadTaskPoService.getById(uuid) != null) {
                    log.warn("已经有相同任务 pid = {}", illust.getId());
                    return 0;
                }
                final String oUrl = illust.getOriginalUrl().replace("_p0", "_p" + i);
                final String suffix = oUrl.substring(oUrl.lastIndexOf('.'));
                final String rUrl = PIXIV_RE_DOMAIN + illust.getId() + (i > 0 ? ("-" + i) : "") + suffix;
                final String rUrl2 = oUrl.replace("i.pximg.net", PIXIV_RE_DOMAIN_2);
                final String filename = oUrl.substring(oUrl.lastIndexOf("/") + 1);

                Aria2DownloadTaskPo task = new Aria2DownloadTaskPo();
                task.setDir(downloadPath);
                task.setFileName(filename);
                task.setUrls(List.of(oUrl, rUrl, rUrl2));
                task.setUuid(uuid);
                task.setType("pixiv-插画/漫画");
                task.setPriority(1);
                task.setTimestamp(ZonedDateTime.now().toEpochSecond());
                taskList.add(task);
            }
            log.info("添加 {} 个 插画/漫画任务 {}", taskList.size(), illust.getId());
            aria2DownloadTaskPoService.saveBatch(taskList);
            return taskList.size();
        }
    }
}