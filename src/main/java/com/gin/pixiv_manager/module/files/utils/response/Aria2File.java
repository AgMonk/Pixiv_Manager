package com.gin.pixiv_manager.module.files.utils.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Aria2文件
 *
 * @author bx002
 * @date 2021/2/3 16:08
 */
@Data
public class Aria2File implements Serializable {
    String path;
//    @JSONField(serialize = false)
    List<Aria2Uri> uris;
    Long completedLength;
    Long length;
    Integer index;
    Boolean selected;
}
