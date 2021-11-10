package com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @author bx002
 */
@Data
public class PixivTag implements Serializable {
    String tag;
    HashMap<String,String> translation;

    public String getTagTranslation(){
        if (translation==null || translation.size()==0) {
            return null;
        }
        return translation.get("en");
    }
}
