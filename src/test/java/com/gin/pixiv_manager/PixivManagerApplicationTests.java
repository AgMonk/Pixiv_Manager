package com.gin.pixiv_manager;

import com.gin.pixiv_manager.module.aria2.service.Aria2DownloadTaskPoService;
import com.gin.pixiv_manager.module.pixiv.bo.TagDictionary;
import com.gin.pixiv_manager.module.pixiv.service.PixivCookieService;
import com.gin.pixiv_manager.module.pixiv.service.PixivIllustPoService;
import com.gin.pixiv_manager.module.pixiv.service.PixivIllustTagPoService;
import com.gin.pixiv_manager.module.pixiv.service.PixivTagPoService;
import com.gin.pixiv_manager.sys.utils.JsonUtil;
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
        final long pid = 94046810;
        illustPoService.findIllust(pid, ZonedDateTime.now().minusHours(1).toEpochSecond()).get(1, TimeUnit.MINUTES);
        final Future<PixivResBookmarksAdd> future = illustPoService.addTag(pid);
        final PixivResBookmarksAdd res = future.get(10, TimeUnit.SECONDS);

//        final List<String> illustTagNames = pixivIllustTagPoService.listTagByPid(94079420L);
//        if (illustTagNames.size() == 0) {
//            throw new BusinessException(4000, "没有Tag数据，请先请求详情");
//        }
//        final HashSet<PixivTagPo> pixivTagPos = pixivTagPoService.listSimplified(illustTagNames);

        JsonUtil.printJson(new TagDictionary(pixivTagPoService));
    }

}
