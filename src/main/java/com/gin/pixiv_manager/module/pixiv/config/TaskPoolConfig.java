package com.gin.pixiv_manager.module.pixiv.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import static com.gin.pixiv_manager.sys.config.TaskExecutePool.getExecutor;

/**
 * @author bx002
 */
@Configuration
public class TaskPoolConfig {
    @Bean
    public ThreadPoolTaskExecutor illustExecutor() {
        return getExecutor("illust", 1);
    }

}
