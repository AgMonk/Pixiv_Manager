package com.gin.pixiv_manager.sys.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * 图片工具类
 * @author bx002
 */
public class ImageUtils {
    /**
     * 验证图片是否损坏
     * @return boolean true 正常 false 损坏
     */
    public static boolean verifyImage(File file) {
//            System.out.println("file = " + file);
        try (FileInputStream fis = new FileInputStream(file)) {
            BufferedImage sourceImg = ImageIO.read(fis);
            if (sourceImg == null) {
                return false;
            }
            int picWidth = sourceImg.getWidth();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
