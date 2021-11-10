package com.gin.pixiv_manager.sys.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author bx002
 */
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Data
public class BusinessException extends RuntimeException {
    int code;
    String message;

    public BusinessException(BusinessExceptionEnum businessExceptionEnum) {
        this.code = businessExceptionEnum.code;
        this.message = businessExceptionEnum.message;
    }
}
