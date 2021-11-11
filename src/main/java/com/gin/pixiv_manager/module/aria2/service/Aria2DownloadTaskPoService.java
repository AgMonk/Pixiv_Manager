package com.gin.pixiv_manager.module.aria2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gin.pixiv_manager.module.aria2.entity.Aria2DownloadTaskPo;
import com.gin.pixiv_manager.module.pixiv.entity.PixivIllustPo;
import com.gin.pixiv_manager.sys.utils.TimeUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.gin.pixiv_manager.module.pixiv.entity.PixivIllustPo.ILLUST_TYPE_GIF;

/**
 * @author bx002
 */
@Transactional(rollbackFor = Exception.class)
public interface Aria2DownloadTaskPoService extends IService<Aria2DownloadTaskPo> {
    String rootPath = "f:/illust";
    String pixivReDomain = "https://pixiv.re/";

    default void addPixivIllust(PixivIllustPo illust) {
        String pixivPath = rootPath + "/pixiv/" + TimeUtils.DATE_FORMATTER.format(ZonedDateTime.now());

        if (ILLUST_TYPE_GIF.equals(illust.getType())) {//                动图 添加一个任务
            String filename = illust.getOriginalUrl();
            filename = filename.substring(filename.lastIndexOf("/") + 1);
            Aria2DownloadTaskPo task = new Aria2DownloadTaskPo();
            task.setDir(pixivPath);
            task.setFileName(filename);
            task.setUrls(Collections.singletonList(illust.getOriginalUrl()));
            task.createUuid();
            task.setType("pixiv-gif");
            task.setPriority(2);
            save(task);
        } else {//                其他 可能添加多个任务
            List<Aria2DownloadTaskPo> taskList = new ArrayList<>();
            for (int i = 0; i < illust.getPageCount(); i++) {
                final String oUrl = illust.getOriginalUrl().replace("_p0", "_p" + i);
                final String suffix = oUrl.substring(oUrl.lastIndexOf('.'));
                final String rUrl = pixivReDomain + illust.getId() + (i > 0 ? ("-" + i) : "") + suffix;
                final String filename = oUrl.substring(oUrl.lastIndexOf("/") + 1);

                Aria2DownloadTaskPo task = new Aria2DownloadTaskPo();
                task.setDir(pixivPath);
                task.setFileName(filename);
                task.setUrls(List.of(oUrl, rUrl));
                task.createUuid();
                task.setType("pixiv-插画/漫画");
                task.setPriority(1);
                taskList.add(task);
            }
            saveBatch(taskList);
        }
    }
}