package com.gin.pixiv_manager.sys.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.gin.pixiv_manager.sys.exception.BusinessExceptionEnum.*;
import static com.gin.pixiv_manager.sys.utils.StringUtils.getProjectName;
import static com.gin.pixiv_manager.sys.utils.StringUtils.isWindows;

/**
 * 需要保存上传文件的业务
 * @author bx002
 */

public interface AttachmentService {

    org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AttachmentService.class);

    /**
     * 生成一个以 项目名 、类名 构成的目录路径
     * @return 目录路径
     */
    default String getServicePath() {
        return String.format("%s/home/%s/files/%s", isWindows() ? "d:" : "", getProjectName(), this.getClass().getName());
    }


    /**
     * 以默认方式保存一个文件
     * @param file 上传文件
     * @param uuid id
     * @return 文件
     * @throws IOException 异常
     */
    default File save(MultipartFile file, String uuid) throws IOException {
        return saveFile(file, String.format("%s/%s/%s", getServicePath(), uuid, getFileName(file)));
    }

    /**
     * 获得保存的文件名 默认使用上传文件的原始名称
     * @param file 上传文件
     * @return 文件名
     */
    default String getFileName(MultipartFile file) {
        return file.getOriginalFilename();
    }

    /**
     * 以默认方式读取一个目录的文件列表
     * @param uuid uuid
     * @return 文件列表
     */
    default List<File> list(String uuid) {
        return listFiles(String.format("%s/%s", getServicePath(), uuid));
    }


    default boolean delete(String uuid,String fileName){
        return deleteFile(String.format("%s/%s/%s",getServicePath(),uuid,fileName));
    }




    /**
     * @param file 上传文件
     * @param path 保存路径
     * @return 目标文件
     * @throws IOException 异常
     */
    default File saveFile(MultipartFile file, String path) throws IOException {
        return saveFile(file, new File(path));
    }

    /**
     * 保存附件
     * @param file     上传文件
     * @param destFile 目标保存位置
     * @return 目标文件
     * @throws IOException 异常
     */
    default File saveFile(MultipartFile file, File destFile) throws IOException {
        FILE_IS_NULL.assertNotNull(file);
        FILE_IS_NULL.assertNotNull(destFile);

        File parentFile = destFile.getParentFile();

        if (!parentFile.exists()) {
            FILE_CREATE_FAILED.assertTrue(parentFile.mkdirs());
        }
        file.transferTo(destFile);

        LOG.info("{} 保存文件 {}", this.getClass().getName(), destFile.getPath());
        return destFile;
    }

    /**
     * 删除文件
     * @param file 文件
     * @return 是否删除成功
     */
    default boolean deleteFile(File file) {
        FILE_IS_NULL.assertNotNull(file);
        FILE_NOT_EXISTS.assertTrue(file.exists());
        return file.delete();
    }

    /**
     * 删除文件
     * @param path 文件路径
     * @return 是否删除成功
     */
    default boolean deleteFile(String path) {
        return deleteFile(new File(path));
    }

    /**
     * 查询指定目录下的文件列表
     * @param file 目录
     * @return 文件列表
     */
    default List<File> listFiles(File file) {
        FILE_IS_NULL.assertNotNull(file);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        FILE_IS_NOT_DIR.assertTrue(file.isDirectory());

        File[] files = file.listFiles();
        if (files == null) {
            return new ArrayList<>();
        }
        return Arrays.asList(files);
    }

    /**
     * 查询指定目录下的文件列表
     * @param path 目录路径
     * @return 文件列表
     */
    default List<File> listFiles(String path) {
        return listFiles(new File(path));
    }
}
