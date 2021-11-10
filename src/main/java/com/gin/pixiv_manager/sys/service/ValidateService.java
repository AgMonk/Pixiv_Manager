package com.gin.pixiv_manager.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;

import static com.gin.pixiv_manager.sys.exception.BusinessExceptionEnum.RECORD_UUID_EXISTS;
import static com.gin.pixiv_manager.sys.exception.BusinessExceptionEnum.RECORD_UUID_NOT_EXISTS;

/**
 * @author bx002
 */
public interface ValidateService<T> extends IService<T> {

    /**
     * 断言uuid不存在
     * @param uuid uuid
     * @return void
     * @author bx002
     * @date 2021/7/10 11:49
     */
    default void assertUuidNotExits(String uuid){
        RECORD_UUID_NOT_EXISTS.assertNull(getById(uuid));
    }
    /**
     * 断言uuid已存在
     * @param uuid uuid
     * @return void
     * @author bx002
     * @date 2021/7/10 11:49
     */
    default void assertUuidExits(String uuid){
        RECORD_UUID_EXISTS.assertNotNull(getById(uuid));
    }
}
