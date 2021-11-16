package com.gin.pixiv_manager.sys.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件操作工具类
 * @author bx002
 */
public class FileUtils {
    public static List<File> listAllFiles(String path) throws IOException {
        return listAllFiles(new File(path));
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

    public static void assertFileExists(File file) throws IOException {
        if (!file.exists()) {
            throw new IOException("指定文件不存在：" + file.getPath());
        }
    }

    public static void assertFileIsDir(File file) throws IOException {
        if (!file.isDirectory()) {
            throw new IOException("指定文件不是目录：" + file.getPath());
        }
    }
}