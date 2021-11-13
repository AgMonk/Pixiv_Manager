package com.gin.pixiv_manager.module.pixiv.service.task;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gin.pixiv_manager.module.aria2.service.Aria2DownloadTaskPoService;
import com.gin.pixiv_manager.module.pixiv.dao.PixivUntaggedIllustTaskPoDao;
import com.gin.pixiv_manager.module.pixiv.entity.PixivCookie;
import com.gin.pixiv_manager.module.pixiv.entity.PixivIllustPo;
import com.gin.pixiv_manager.module.pixiv.entity.tasks.PixivUntaggedIllustTaskPo;
import com.gin.pixiv_manager.module.pixiv.service.PixivCookieService;
import com.gin.pixiv_manager.module.pixiv.service.PixivIllustPoService;
import com.gin.pixiv_manager.module.pixiv.utils.pixiv.request.PixivRequest;
import com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.body.PixivSearchIllustManga;
import com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.entity.PixivIllust;
import com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.res.PixivResBookmarks;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

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
    private final Aria2DownloadTaskPoService aria2DownloadTaskPoService;

    public static final int MAX_ACTIVE_TASKS = 5;
    private final List<Long> activeTasks = new ArrayList<>();

    @Override
    @Scheduled(cron = "0 0/5 * * * ?")
    public void findBookmarks() throws IOException {
        final PixivCookie pixivCookie = pixivCookieService.get();

        final PixivResBookmarks untagged = PixivRequest.findBookmarks(pixivCookie.getCookie(), pixivCookie.getUid(), 0, 10, "未分類");

        if (untagged.getError()) {
            log.error(untagged.getMessage());
            return;
        }
        final PixivSearchIllustManga body = untagged.getBody();
        log.info("未分类作品剩余 {} 个", body.getTotal());
        final List<Long> pidList = body.getData().stream()
                .map(PixivIllust::getId).collect(Collectors.toList());

        final List<Long> existsPid = listByIds(pidList).stream().map(PixivUntaggedIllustTaskPo::getPid).collect(Collectors.toList());

        pidList.removeAll(existsPid);

        if (pidList.size() > 0) {
            saveBatch(pidList.stream().map(PixivUntaggedIllustTaskPo::new).collect(Collectors.toList()));
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
        bookmarkExecutor.execute(() -> {
            List<Future<PixivIllustPo>> detailsTask = new ArrayList<>();
            for (Long pid : pidList) {
                activeTasks.add(pid);
                detailsTask.add(illustPoService.findIllust(pid, time));
            }
            for (Future<PixivIllustPo> future : detailsTask) {
                PixivIllustPo illust = null;
                for (int i = 0; i < 10; i++) {
                    try {
                        illust = future.get(1, TimeUnit.MINUTES);
                        break;
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        log.info("第 {} 次请求超时 继续尝试", i + 1);
                        e.printStackTrace();
                    }
                }
                if (illust == null) {
                    future.cancel(true);
                    continue;
                }
                aria2DownloadTaskPoService.addPixivIllust(illust);
                final Long pid = illust.getId();
                removeById(pid);
                activeTasks.remove(pid);
                try {
                    illustPoService.addTag(pid).get(30, TimeUnit.SECONDS);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    e.printStackTrace();
                }
            }
        });

    }
}