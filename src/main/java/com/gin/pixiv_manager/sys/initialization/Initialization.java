package com.gin.pixiv_manager.sys.initialization;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Map;

/**
 * 初始化
 * @author bx002
 */
@Component
@Slf4j
public class Initialization implements ApplicationContextAware, ApplicationRunner {
    public static ApplicationContext context;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Initialization.context = applicationContext;
    }

    @Override
    public void run(ApplicationArguments args) {
    }
}
