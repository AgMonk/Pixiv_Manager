package com.gin.pixiv_manager;

import com.gin.pixiv_manager.module.aria2.service.Aria2DownloadTaskPoService;
import com.gin.pixiv_manager.module.pixiv.service.IllustPoService;
import com.gin.pixiv_manager.module.pixiv.service.PixivCookieService;
import com.gin.pixiv_manager.module.pixiv.service.PixivIllustTagPoService;
import com.gin.pixiv_manager.module.pixiv.service.PixivTagPoService;
import com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.res.PixivResBookmarksAdd;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@SuppressWarnings("RedundantThrows")
@SpringBootTest
class PixivManagerApplicationTests {
    @Autowired
    PixivCookieService pixivCookieService;
    @Autowired
    IllustPoService illustPoService;
    @Autowired
    Aria2DownloadTaskPoService aria2DownloadTaskPoService;
    @Autowired
    PixivIllustTagPoService pixivIllustTagPoService;
    @Autowired
    PixivTagPoService pixivTagPoService;

    @Test
    void contextLoads() throws IOException, ExecutionException, InterruptedException, TimeoutException {
        illustPoService.findIllust(94079420L, ZonedDateTime.now().minusHours(1).toEpochSecond()).get(1, TimeUnit.MINUTES);
        final Future<PixivResBookmarksAdd> future = illustPoService.addTag(94079420L);
        final PixivResBookmarksAdd res = future.get(2, TimeUnit.MINUTES);

//        final List<String> illustTagNames = pixivIllustTagPoService.listTagByPid(94079420L);
//        if (illustTagNames.size() == 0) {
//            throw new BusinessException(4000, "没有Tag数据，请先请求详情");
//        }
//        final HashSet<PixivTagPo> pixivTagPos = pixivTagPoService.listSimplified(illustTagNames);
    }

}
