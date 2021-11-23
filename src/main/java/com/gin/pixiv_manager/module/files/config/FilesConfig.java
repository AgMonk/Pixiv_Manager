package com.gin.pixiv_manager.module.files.config;

import com.gin.pixiv_manager.module.files.config.child.Aria2Config;
import com.gin.pixiv_manager.module.files.config.child.PixivConfig;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Aria2配置
 * @author bx002
 */
@Component
@ConfigurationProperties(prefix = "files")
@Getter
@Setter
public class FilesConfig {
    String rootPath;

    PixivConfig pixiv;

    Aria2Config aira2;
}
