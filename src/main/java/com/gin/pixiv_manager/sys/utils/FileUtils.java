package com.gin.pixiv_manager.sys.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.gin.pixiv_manager.sys.exception.BusinessExceptionEnum.*;

/**
 * 文件操作工具类
 * @author bx002
 */
@Slf4j
public class FileUtils {

    public static File saveMultipartFile(MultipartFile file, File destFile) throws IOException {
        FILE_IS_NULL.assertNotNull(file);
        FILE_IS_NULL.assertNotNull(destFile);

        File parentFile = destFile.getParentFile();

        if (!parentFile.exists()) {
            FILE_CREATE_FAILED.assertTrue(parentFile.mkdirs());
        }
        file.transferTo(destFile);

        log.info("已保存文件 {}", destFile.getPath());
        return destFile;
    }


    public static List<File> listAllFiles(String path) throws IOException {
        return listAllFiles(new File(path));
    }

    /**
     * 删除文件
     * @param file 文件
     * @return 是否删除成功
     */
    public static boolean deleteFile(File file) {
        FILE_IS_NULL.assertNotNull(file);
        FILE_NOT_EXISTS.assertTrue(file.exists());
        final boolean delete = file.delete();
        log.info("删除文件 {} {}", delete ? "成功" : "失败", file);
        return delete;
    }

    public static void move(File src, String destPath) throws IOException {
        move(src, new File(destPath + "/" + src.getName()));
    }

    public static void move(Collection<File> files, String destPath) throws IOException {
        for (File file : files) {
            move(file, new File(destPath + "/" + file.getName()));
        }
    }

    public static void move(File src, File dest) throws IOException {
        assertFileExists(src);
        assertFileNotExists(dest);
        mkDirs(dest.getParentFile());
        if (!src.renameTo(dest)) {
            throw new IOException(String.format("移动失败 %s -> %s", src.getPath(), dest.getPath()));
        }
        log.info("已移动文件 {} -> {}", src.getPath(), dest.getPath());
    }

    public static List<File> listAllFiles(File dir) throws IOException {
        assertFileExists(dir);
        assertFileIsDir(dir);
        final List<File> allFiles = new ArrayList<>();
        final List<File> files = listFiles(dir);
        for (File file : files) {
            if (file.isDirectory()) {
                allFiles.addAll(listAllFiles(file));
            } else {
                allFiles.add(file);
            }
        }
        return allFiles;
    }

    public static List<File> listFiles(File dir) throws IOException {
        assertFileExists(dir);
        assertFileIsDir(dir);
        final File[] files = dir.listFiles();
        if (files == null) {
            return new ArrayList<>();
        }
        return Arrays.stream(files).collect(Collectors.toList());
    }

    public static String deleteIllegalChar(String s) {
        return s
                .replace("?", "_")
                .replace("|", "_")
                .replace("<", "_")
                .replace(">", "_")
                .replace("\"", "_")
                .replace("*", "_")
                .replace(":", "_")
//                .replace("/","_")
//                .replace("\\","_")
                ;
    }

    public static void mkDirs(File dir) throws IOException {
        if (dir.exists() && !dir.isDirectory()) {
            throw new IOException("该文件已存在且不是目录: " + dir.getPath());
        }

        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("目录创建失败: " + dir.getPath());
        }
    }

    public static void assertFileExists(File file) throws IOException {
        if (!file.exists()) {
            throw new IOException("指定文件不存在：" + file.getPath());
        }
    }

    public static void assertFileNotExists(File file) throws IOException {
        if (file.exists()) {
            throw new IOException("指定文件已存在：" + file.getPath());
        }
    }

    public static void assertFileIsDir(File file) throws IOException {
        if (!file.isDirectory()) {
            throw new IOException("指定文件不是目录：" + file.getPath());
        }
    }
}
