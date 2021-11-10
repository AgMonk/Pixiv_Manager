package com.gin.pixiv_manager;

import com.gin.pixiv_manager.module.pixiv.service.PixivCookieService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class PixivManagerApplicationTests {
    @Autowired
    PixivCookieService pixivCookieService;

    @Test
    void contextLoads() throws IOException {
    }

}
