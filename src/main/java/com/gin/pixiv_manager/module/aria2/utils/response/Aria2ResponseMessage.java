package com.gin.pixiv_manager.module.aria2.utils.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author bx002
 * @date 2021/2/3 16:39
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class Aria2ResponseMessage extends Aria2Response implements Serializable {
    String result;
}
