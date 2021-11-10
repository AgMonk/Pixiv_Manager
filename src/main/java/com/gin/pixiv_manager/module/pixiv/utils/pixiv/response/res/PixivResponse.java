package com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.res;

import com.gin.pixiv_manager.sys.utils.TimeUtils;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZonedDateTime;

/**
 * Pixiv返回对象
 * @author bx002
 */
@Data
@ApiModel("PixivCookie")
public class PixivResponse<T> implements Serializable {
    Boolean error;
    String message;
    Long timestamp;
    T body;

    public String getTime(){
        return TimeUtils.DATE_TIME_FORMATTER.format(ZonedDateTime.ofInstant(Instant.ofEpochSecond(this.timestamp),TimeUtils.DEFAULT_ZONE_ID));
    }

    public PixivResponse() {
        this.timestamp = ZonedDateTime.now().toEpochSecond();
    }
}
