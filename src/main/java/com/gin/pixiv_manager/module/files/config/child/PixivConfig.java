package com.gin.pixiv_manager.module.files.config.child;

import lombok.Data;

import java.io.Serializable;

/**
 * Pixiv运行参数
 * @author bx002
 */
@Data
public class PixivConfig implements Serializable {
    /**
     * pixiv根文件夹名
     */
    String rootPath;
    /**
     * 未分类文件夹名
     */
    String untaggedDir;
    /**
     * 已分类文件夹名
     */
    String taggedDir;
    /**
     * 重新录入文件夹名
     */
    String reEntryDir;
    /**
     * 损坏文文件夹名
     */
    String errorDir;
    /**
     * 档案已被删除文件夹名
     */
    String deletedDir;
}
