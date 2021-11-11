package com.gin.pixiv_manager;

import com.gin.pixiv_manager.module.pixiv.entity.PixivIllustPo;
import com.gin.pixiv_manager.module.pixiv.service.IllustPoService;
import com.gin.pixiv_manager.sys.utils.JsonUtil;
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
    IllustPoService illustPoService;

    @Test
    void contextLoads() throws IOException, ExecutionException, InterruptedException, TimeoutException {
        final Future<PixivIllustPo> illust = illustPoService.findIllust(92670966L, ZonedDateTime.now().toEpochSecond());
        final PixivIllustPo pixivIllustPo = illust.get(5, TimeUnit.MINUTES);
        JsonUtil.printJson(pixivIllustPo);
    }

}
