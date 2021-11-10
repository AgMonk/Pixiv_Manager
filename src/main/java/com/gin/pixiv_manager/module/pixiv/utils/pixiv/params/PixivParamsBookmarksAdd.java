package com.gin.pixiv_manager.module.pixiv.utils.pixiv.params;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author bx002
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PixivParamsBookmarksAdd implements Serializable {
    @JSONField(name = "illust_id")
    Long pid;
    int restrict = 0;
    String comment = "";
    List<String> tags = new ArrayList<>();

    public PixivParamsBookmarksAdd(Long pid, List<String> tags) {
        this.pid = pid;
        this.tags = tags;
    }
}
