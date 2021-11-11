package com.gin.pixiv_manager.module.aria2.utils.method;

import com.gin.pixiv_manager.module.aria2.utils.request.Aria2RequestParam;

import java.io.Serializable;

/**
 * Aria2方法
 *
 * @author bx002
 * @date 2021/2/3 15:23
 */
public enum Aria2Method implements Serializable {
    /**
     * 获取活动任务
     */
    TELL_ACTIVE("aria2.tellActive"),
    /**
     * 添加任务
     */
    ADD_URI("aria2.addUri"),
    /**
     * 获取总状态
     */
    GET_GLOBAL_STAT("aria2.getGlobalStat"),
    /**
     * 获取停止任务
     */
    TELL_STOPPED("aria2.tellStopped"),
    /**
     * 获取等待队列任务
     */
    TELL_WAITING("aria2.tellWaiting"),
    /**
     * 移除任务
     */
    REMOVE_DOWNLOAD_RESULT("aria2.removeDownloadResult"),
    ;


    private String name;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Aria2RequestParam toParam(){
        final Aria2RequestParam param = new Aria2RequestParam();
        param.setMethod(this);
        param.createUuid();
        return param;
    }

    Aria2Method(String name) {
        this.name = name;
    }
}
