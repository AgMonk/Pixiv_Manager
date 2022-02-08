package com.gin.pixiv_manager.module.files.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gin.pixiv_manager.module.files.config.FilesConfig;
import com.gin.pixiv_manager.module.files.entity.Aria2DownloadTaskPo;
import com.gin.pixiv_manager.module.files.utils.request.Aria2Request;
import com.gin.pixiv_manager.module.files.utils.response.Aria2Quest;
import com.gin.pixiv_manager.sys.utils.StringUtils;
import org.apache.http.conn.HttpHostConnectException;
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
     * 定时检查任务情况
     * @throws IOException 异常
     */
    @Scheduled(cron = "0/10 * * * * ?")
    default void check() throws IOException {
        try {
            final List<String> gidList = listAllGid();
//        查询已完成且来自本系统的任务
            removeCompletedTasks(gidList);

            final List<Aria2Quest> activeQuest = Aria2Request.tellActive().getResult();
            final List<Aria2Quest> waitingQuest = Aria2Request.tellWaiting().getResult();

            resetDeletedQuest(gidList, activeQuest, waitingQuest);

            int count = getConfig().getAira2().getMaxTasks() - activeQuest.size() - waitingQuest.size();

            if (count <= 0) {
                //        队列数量较多 不添加新任务
                return;
            }
//        提交下载
            addQuests(count);
        } catch (HttpHostConnectException e) {
            LOG.error(e.getMessage());
        }
    }

    /**
     * 添加指定个数的下载任务
     * @param count 数量
     */
    private void addQuests(int count) {
        final QueryWrapper<Aria2DownloadTaskPo> qw = new QueryWrapper<>();
        qw.isNull("gid");
        qw.orderByDesc("priority");
        qw.orderByAsc("timestamp");
        qw.last("limit " + count);
        final List<Aria2DownloadTaskPo> tasks = new ArrayList<>();
        for (Aria2DownloadTaskPo aria2DownloadTaskPo : list(qw)) {
            try {
                Aria2DownloadTaskPo execute = aria2DownloadTaskPo.execute();
                tasks.add(execute);
            } catch (IOException e) {
                LOG.warn("添加任务失败 {}", aria2DownloadTaskPo.getFileName());
                LOG.warn(e.getMessage());
            }
        }
        if (tasks.size() > 0) {
            LOG.info("开始执行 {} 个 任务", tasks.size());
            updateBatchById(tasks);
        }
    }

    /**
     * 检查是否存在在列表中，但没有出现在正在下载或队列中的任务；如果存在，则认为这个任务已经被删除需要重新下载，重置它的gid字段
     * @param gidList      应该被下载的gid列表
     * @param activeQuest  正在下载的任务
     * @param waitingQuest 在队列中等待的任务
     */
    private void resetDeletedQuest(List<String> gidList, List<Aria2Quest> activeQuest, List<Aria2Quest> waitingQuest) {
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
    }

    /**
     * 移除已完成的任务
     */
    private void removeCompletedTasks(List<String> gidList) throws IOException {
        final List<Aria2Quest> stopQuest = Aria2Request.tellStop().getResult();
        final List<String> completedQuest = stopQuest.stream()
                .filter(Aria2Quest::isCompleted)
                .map(Aria2Quest::getGid)
                .filter(gidList::contains)
                .collect(Collectors.toList());
        if (completedQuest.size() > 0) {
            LOG.info("移除 {} 个 已完成任务", completedQuest.size());
            List<String> list = new ArrayList<>();
            for (String s : completedQuest) {
                try {
                    Aria2Request.removeQuest(s);
                    list.add(s);
                } catch (IOException e) {
                    LOG.warn("下载任务移除失败 {}", s);
                }
            }
            removeByGid(list);
            gidList.removeAll(completedQuest);
        }
    }

    private void removeByGid(List<String> list) {
        if (StringUtils.isEmpty(list)) {
            return;
        }
        final QueryWrapper<Aria2DownloadTaskPo> qw2 = new QueryWrapper<>();
        qw2.in("gid", list);
        remove(qw2);
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