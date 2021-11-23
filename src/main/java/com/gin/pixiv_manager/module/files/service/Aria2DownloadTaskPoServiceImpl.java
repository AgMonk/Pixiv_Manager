package com.gin.pixiv_manager.module.files.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gin.pixiv_manager.module.files.config.FilesConfig;
import com.gin.pixiv_manager.module.files.dao.Aria2DownloadTaskPoDao;
import com.gin.pixiv_manager.module.files.entity.Aria2DownloadTaskPo;
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

    private final FilesConfig filesConfig;
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
    public FilesConfig getConfig() {
        return filesConfig;
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
        log.info("遍历所有文件 开始");
        this.allFiles = FileUtils.listAllFiles(getRootPath());
        log.info("遍历所有文件 完毕 文件总计 {} 个", this.allFiles.size());
    }

    private String getRootPath() {
        return getConfig().getRootPath();
    }




}