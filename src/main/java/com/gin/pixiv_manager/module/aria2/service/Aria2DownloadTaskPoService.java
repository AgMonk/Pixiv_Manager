package com.gin.pixiv_manager.module.aria2.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gin.pixiv_manager.module.aria2.entity.Aria2DownloadTaskPo;
import com.gin.pixiv_manager.module.aria2.utils.request.Aria2Request;
import com.gin.pixiv_manager.module.aria2.utils.response.Aria2Quest;
import com.gin.pixiv_manager.module.pixiv.bo.TagAnalysisResult;
import com.gin.pixiv_manager.module.pixiv.entity.PixivIllustPo;
import com.gin.pixiv_manager.sys.utils.FileUtils;
import com.gin.pixiv_manager.sys.utils.TimeUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.gin.pixiv_manager.module.pixiv.entity.PixivIllustPo.*;

/**
 * @author bx002
 */
@Transactional(rollbackFor = Exception.class)
public interface Aria2DownloadTaskPoService extends IService<Aria2DownloadTaskPo> {
    org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Aria2DownloadTaskPoService.class);

    String PIXIV_RE_DOMAIN = "https://pixiv.re/";
    String PIXIV_RE_DOMAIN_2 = "i.pixiv.re";
    int MAX_TASKS = 10;

    String getRootPath();

    /**
     * 下载一个Pixiv作品
     * @param illust 作品详情
     */
    default void addPixivIllust(PixivIllustPo illust) {
        String pixivPath = getRootPath() + "/pixiv/待归档/" + TimeUtils.DATE_FORMATTER.format(ZonedDateTime.now());

        //                动图 添加一个任务
        if (ILLUST_TYPE_GIF.equals(illust.getType())) {
            String uuid = illust.getId() + "_u0";
            if (getById(uuid) != null) {
                LOG.warn("已经有相同任务 pid = {}", illust.getId());
                return;
            }
            final String oUrl = illust.getOriginalUrl();
            final String rUrl2 = oUrl.replace("i.pximg.net", PIXIV_RE_DOMAIN_2);
            String filename = oUrl.substring(oUrl.lastIndexOf("/") + 1);
            Aria2DownloadTaskPo task = new Aria2DownloadTaskPo();
            task.setDir(pixivPath);
            task.setFileName(filename);
            task.setUrls(List.of(oUrl, rUrl2));
            task.setUuid(uuid);
            task.setType("pixiv-gif");
            task.setPriority(2);
            task.setTimestamp(ZonedDateTime.now().toEpochSecond());
            save(task);
            LOG.info("添加 1 个 动图任务 {}", illust.getId());
        } else {
            //                其他 可能添加多个任务
            List<Aria2DownloadTaskPo> taskList = new ArrayList<>();
            for (int i = 0; i < illust.getPageCount(); i++) {
                final String uuid = illust.getId() + "_p" + i;
                if (getById(uuid) != null) {
                    LOG.warn("已经有相同任务 pid = {}", illust.getId());
                    return;
                }
                final String oUrl = illust.getOriginalUrl().replace("_p0", "_p" + i);
                final String suffix = oUrl.substring(oUrl.lastIndexOf('.'));
                final String rUrl = PIXIV_RE_DOMAIN + illust.getId() + (i > 0 ? ("-" + i) : "") + suffix;
                final String rUrl2 = oUrl.replace("i.pximg.net", PIXIV_RE_DOMAIN_2);
                final String filename = oUrl.substring(oUrl.lastIndexOf("/") + 1);

                Aria2DownloadTaskPo task = new Aria2DownloadTaskPo();
                task.setDir(pixivPath);
                task.setFileName(filename);
                task.setUrls(List.of(oUrl, rUrl, rUrl2));
                task.setUuid(uuid);
                task.setType("pixiv-插画/漫画");
                task.setPriority(1);
                task.setTimestamp(ZonedDateTime.now().toEpochSecond());
                taskList.add(task);
            }
            LOG.info("添加 {} 个 插画/漫画任务 {}", taskList.size(), illust.getId());
            saveBatch(taskList);
        }
    }

    /**
     * 移除已完成的任务
     */
    @Scheduled(cron = "0/10 * * * * ?")
    default void removeCompletedTask() {
        final List<String> gidList = listAllGid();
//        查询已完成且来自本系统的任务
        final List<Aria2Quest> stopQuest = Aria2Request.tellStop().getResult();
        final List<String> completedQuest = stopQuest.stream()
                .filter(Aria2Quest::isCompleted)
                .map(Aria2Quest::getGid)
                .filter(gidList::contains)
                .collect(Collectors.toList());
        if (completedQuest.size() > 0) {
            LOG.info("移除 {} 个 已完成任务", completedQuest.size());
            final QueryWrapper<Aria2DownloadTaskPo> qw2 = new QueryWrapper<>();
            qw2.in("gid", completedQuest);
            remove(qw2);
            completedQuest.forEach(Aria2Request::removeQuest);
            gidList.removeAll(completedQuest);
        }

        final List<Aria2Quest> activeQuest = Aria2Request.tellActive().getResult();
        final List<Aria2Quest> waitingQuest = Aria2Request.tellWaiting().getResult();

//        发现已被删除的任务
        List<Aria2Quest> quests = new ArrayList<>(activeQuest);
        quests.addAll(waitingQuest);
        gidList.removeAll(quests.stream().map(Aria2Quest::getGid).collect(Collectors.toList()));

        if (gidList.size() > 0) {
//            有任务被删除 重新下载
            final UpdateWrapper<Aria2DownloadTaskPo> uw = new UpdateWrapper<>();
            uw.in("gid", gidList);
            uw.set("gid", null);
            update(uw);
        }

        int count = MAX_TASKS - activeQuest.size() - waitingQuest.size();

        if (count <= 0) {
//        队列数量较多 不添加新任务
            return;
        }
//        提交下载
        final QueryWrapper<Aria2DownloadTaskPo> qw = new QueryWrapper<>();
        qw.isNull("gid");
        qw.orderByDesc("priority");
        qw.orderByAsc("timestamp");
        qw.last("limit " + count);
        final List<Aria2DownloadTaskPo> tasks = list(qw).stream().map(Aria2DownloadTaskPo::execute).collect(Collectors.toList());
        if (tasks.size() > 0) {
            LOG.info("开始执行 {} 个 任务", tasks.size());
            updateBatchById(tasks);
        }

    }


    /**
     * 查询所有gid
     * @return 所有gid
     */
    default List<String> listAllGid() {
        final QueryWrapper<Aria2DownloadTaskPo> qw = new QueryWrapper<>();
        qw.select("gid").isNotNull("gid");
        return list(qw).stream().map(Aria2DownloadTaskPo::getGid).collect(Collectors.toList());
    }

    List<File> getAllFiles(String prefix);

    void updateAllFileList() throws IOException;


    /**
     * 整理Pixiv文件
     */
    default void arrangePixivFiles(String dirName) {
        final List<File> allFiles = getAllFiles("/pixiv/待归档/" + dirName);

        arrangePixivFileWithPattern(allFiles, ILLUST_FILE_NAME_PATTERN);
        arrangePixivFileWithPattern(allFiles, ILLUST_GIF_FILE_NAME_PATTERN);
    }

    /**
     * 从列表中过滤出文件名符合正则表达式的文件，进行标签分析，并移动到指定文件夹
     * @param allFiles 文件列表
     * @param pattern  正则
     */
    private void arrangePixivFileWithPattern(List<File> allFiles, Pattern pattern) {
        final Map<Long, List<File>> filesMap = allFiles.stream()
                .filter(file -> pattern.matcher(file.getName()).find()).collect(Collectors.groupingBy(file -> {
                    final Matcher matcher = pattern.matcher(file.getName());
                    if (matcher.find()) {
                        return Long.parseLong(matcher.group(1));
                    }
                    return 0L;
                }));
        if (filesMap.size() == 0) {
            return;
        }
        filesMap.forEach((pid, files) -> {
            final TagAnalysisResult result = getTagAnalysisResultByPid(pid);
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

    /**
     * 根据pid 获取标签分析结果
     * @param pid pid
     * @return 标签分析结果
     */
    TagAnalysisResult getTagAnalysisResultByPid(long pid);
}