package com.gin.pixiv_manager.module.aria2.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Aria2配置
 * @author bx002
 */
@Component
@ConfigurationProperties(prefix = "aria2")
@Getter
@Setter
public class Aria2Config {
    String rootPath;
    Integer maxTasks;
}
