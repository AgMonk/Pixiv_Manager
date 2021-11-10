package com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.body;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @author bx002
 */
@Data
public class PixivSearchResult implements Serializable {
    @JSONField(serialize = false)
    HashMap<String,HashMap<String,String>> tagTranslation;
    PixivSearchIllustManga illustManga;

    public HashMap<String,String> getTranslation(){
        if (tagTranslation==null||tagTranslation.size()==0) {
            return new HashMap<>(0);
        }
        final HashMap<String, String> map = new HashMap<>(tagTranslation.size());
        tagTranslation.keySet().forEach(k->{
            map.put(k,tagTranslation.get(k).get("zh"));
        });
        return map;
    }
}
