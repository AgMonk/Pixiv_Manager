package com.gin.pixiv_manager.module.pixiv.service.task;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gin.pixiv_manager.module.files.service.PixivFilesService;
import com.gin.pixiv_manager.module.pixiv.dao.PixivUntaggedIllustTaskPoDao;
import com.gin.pixiv_manager.module.pixiv.entity.PixivCookie;
import com.gin.pixiv_manager.module.pixiv.entity.PixivIllustPo;
import com.gin.pixiv_manager.module.pixiv.entity.tasks.PixivUntaggedIllustTaskPo;
import com.gin.pixiv_manager.module.pixiv.service.PixivCookieService;
import com.gin.pixiv_manager.module.pixiv.service.PixivIllustPoService;
import com.gin.pixiv_manager.module.pixiv.utils.pixiv.request.PixivRequest;
import com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.body.PixivSearchIllustManga;
import com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.entity.PixivIllust;
import com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.entity.PixivSearchIllust;
import com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.res.PixivResBookmarks;
import com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.res.PixivResBookmarksAdd;
import com.gin.pixiv_manager.sys.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static com.gin.pixiv_manager.module.files.service.PixivFilesServiceImpl.MESSAGE_DELETED;

/**
 * @author bx002
 */
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class PixivUntaggedIllustTaskPoServiceImpl extends ServiceImpl<PixivUntaggedIllustTaskPoDao, PixivUntaggedIllustTaskPo> implements PixivUntaggedIllustTaskPoService {
    private final PixivCookieService pixivCookieService;
    private final ThreadPoolTaskExecutor bookmarkExecutor;
    private final PixivIllustPoService illustPoService;
    private final PixivFilesService pixivFilesService;

    public static final int MAX_ACTIVE_TASKS = 5;
    private final List<Long> activeTasks = new ArrayList<>();

    private Integer untaggedTotal = 0;

    @Override
    public Integer getUntaggedTotal() {
        return untaggedTotal;
    }

    @Override
    @Scheduled(cron = "0 0/5 * * * ?")
    public void findBookmarks() throws IOException {
        try {
            final PixivCookie pixivCookie = pixivCookieService.get();

            final PixivResBookmarks untagged = PixivRequest.findBookmarks(pixivCookie.getCookie(), pixivCookie.getUid(), 0, 30, "未分類");

            if (untagged.getError()) {
                log.error(untagged.getMessage());
                return;
            }
            final PixivSearchIllustManga body = untagged.getBody();
            final Integer total = body.getTotal();
            if (total == 0) {
                return;
            }
            log.info("未分类作品剩余 {} 个", total);
            this.untaggedTotal = total;
            final List<PixivSearchIllust> data = body.getData().stream().filter(i -> {
                if (i.getUserId() == 0) {
                    log.warn("作品已经被删除 移除收藏 pid = " + i.getId());
                    this.untaggedTotal--;
                    bookmarkExecutor.execute(() -> {
                        try {
                            PixivRequest.bookmarksDelete(pixivCookie.getCookie(), pixivCookie.getToken(), i.getBookmarkData().getId());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    return false;
                }
                return true;
            }).collect(Collectors.toList());
            final List<Long> pidList = data.stream()
                    .map(PixivIllust::getId).collect(Collectors.toList());
            final List<Long> existsPid = listByIds(pidList).stream().map(PixivUntaggedIllustTaskPo::getPid).collect(Collectors.toList());

            pidList.removeAll(existsPid);

            if (pidList.size() > 0) {
                saveBatch(pidList.stream().map(PixivUntaggedIllustTaskPo::new).collect(Collectors.toList()));
            }
        } catch (BusinessException e) {
            log.error(String.format("%s - %s", e.getCode(), e.getMessage()));
        }
    }

    @Scheduled(cron = "30 * * * * ?")
    public void execute() {
        if (activeTasks.size() >= MAX_ACTIVE_TASKS) {
            return;
        }
        final List<Long> pidList = listPid(MAX_ACTIVE_TASKS - activeTasks.size(), activeTasks);
        if (pidList.size() == 0) {
            return;
        }
        final long time = ZonedDateTime.now().minusHours(1).toEpochSecond();
//        添加tag
        bookmarkExecutor.execute(() -> {
            Map<Long, Future<PixivIllustPo>> taskMap = new HashMap<>();
            for (Long pid : pidList) {
                activeTasks.add(pid);
                taskMap.put(pid, illustPoService.findIllust(pid, time));
            }
            taskMap.forEach((pid, future) -> {
                PixivIllustPo illust;
                try {
                    illust = future.get(1, TimeUnit.MINUTES);
                    this.untaggedTotal--;
                    pixivFilesService.downloadFile(illust);
                    removeById(pid);
                    activeTasks.remove(pid);
                    final Future<PixivResBookmarksAdd> fu = illustPoService.addTag(pid);
                    try {
                        fu.get(30, TimeUnit.SECONDS);
                    } catch (InterruptedException | ExecutionException | TimeoutException ignored) {
                        fu.cancel(true);
                    } finally {
                        activeTasks.remove(pid);
                    }
                } catch (BusinessException e) {
                    future.cancel(true);
                    activeTasks.remove(pid);
                    final String message = e.getMessage();
                    log.error(message);
                    if (message.startsWith("没有Tag数据，请先请求详情")) {
                        log.warn("重新添加详情任务 pid = {}", pid);
                        saveOrUpdate(new PixivUntaggedIllustTaskPo(pid));
                    }
                } catch (InterruptedException | TimeoutException e) {
                    future.cancel(true);
                    activeTasks.remove(pid);
                } catch (ExecutionException e) {
                    future.cancel(true);
                    if (e.getMessage().contains(MESSAGE_DELETED)) {
                        log.warn(e.getMessage());
                        removeById(pid);
                        activeTasks.remove(pid);
                        this.untaggedTotal--;
                    }
                }

            });
        });

    }
}