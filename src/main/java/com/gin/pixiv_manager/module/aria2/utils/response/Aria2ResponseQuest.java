package com.gin.pixiv_manager.module.aria2.utils.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * @author bx002
 * @date 2021/2/3 16:39
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class Aria2ResponseQuest extends Aria2Response implements Serializable {
    List<Aria2Quest> result;
}
