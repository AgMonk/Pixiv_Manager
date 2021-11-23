package com.gin.pixiv_manager.module.files.service;

import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

/**
 * @author bx002
 */
@Transactional(rollbackFor = Exception.class)
public interface PixivFilesService {
    /**
     * 重新获取已有文件的档案信息 并移动到待归档文件夹
     * @throws IOException 异常
     */
    void reEntryPixiv() throws IOException;

    /**
     * 归档一个指定文件夹的图片
     * @param dirName 文件夹
     * @throws IOException 异常
     */
    void arrangeFiles(String dirName) throws IOException;

}