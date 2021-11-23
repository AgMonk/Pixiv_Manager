package com.gin.pixiv_manager.module.files.config.child;

import lombok.Data;

import java.io.Serializable;

/**
 * Aria2下载配置
 * @author bx002
 */
@Data
public class Aria2Config implements Serializable {
    /**
     * 下载队列最大长度
     */
    Integer maxTasks;
}
