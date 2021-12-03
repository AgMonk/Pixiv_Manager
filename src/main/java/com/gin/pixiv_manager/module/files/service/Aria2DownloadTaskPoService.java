package com.gin.pixiv_manager.module.files.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gin.pixiv_manager.module.files.config.FilesConfig;
import com.gin.pixiv_manager.module.files.entity.Aria2DownloadTaskPo;
import com.gin.pixiv_manager.module.files.utils.request.Aria2Request;
import com.gin.pixiv_manager.module.files.utils.response.Aria2Quest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author bx002
 */
@Transactional(rollbackFor = Exception.class)
public interface Aria2DownloadTaskPoService extends IService<Aria2DownloadTaskPo> {
    org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Aria2DownloadTaskPoService.class);

    /**
     * aria2配置
     * @return aria2配置
     */
    FilesConfig getConfig();

    /**
     * 移除已完成的任务
     */
    @Scheduled(cron = "0/10 * * * * ?")
    default void removeCompletedTask() throws IOException {
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
            for (String s : completedQuest) {
                Aria2Request.removeQuest(s);
            }
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

        int count = getConfig().getAira2().getMaxTasks() - activeQuest.size() - waitingQuest.size();

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
        final List<Aria2DownloadTaskPo> tasks = new ArrayList<>();
        for (Aria2DownloadTaskPo aria2DownloadTaskPo : list(qw)) {
            Aria2DownloadTaskPo execute = aria2DownloadTaskPo.execute();
            tasks.add(execute);
        }
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

    /**
     * 获取文件库中，路径为指定前缀的文件列表
     * @param prefix 前缀
     * @return 文件列表
     * @throws IOException 异常
     */
    List<File> getAllFiles(String prefix) throws IOException;

    /**
     * 更新文件库
     * @throws IOException 异常
     */
    void updateAllFileList() throws IOException;


}