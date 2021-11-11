package com.gin.pixiv_manager;

import com.gin.pixiv_manager.module.aria2.service.Aria2DownloadTaskPoService;
import com.gin.pixiv_manager.module.pixiv.service.IllustPoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@SuppressWarnings("RedundantThrows")
@SpringBootTest
class PixivManagerApplicationTests {
    @Autowired
    IllustPoService illustPoService;
    @Autowired
    Aria2DownloadTaskPoService aria2DownloadTaskPoService;

    @Test
    void contextLoads() throws IOException, ExecutionException, InterruptedException, TimeoutException {
//        final Future<PixivIllustPo> illust = illustPoService.findIllust(93180575L, ZonedDateTime.now().toEpochSecond());
////        final Future<PixivIllustPo> illust = illustPoService.findIllust(92670966L, ZonedDateTime.now().toEpochSecond());
//        final PixivIllustPo pixivIllustPo = illust.get(5, TimeUnit.MINUTES);

//        aria2DownloadTaskPoService.addPixivIllust(pixivIllustPo);
        aria2DownloadTaskPoService.removeCompletedTask();
    }

}
