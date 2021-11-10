package com.gin.pixiv_manager.sys.response;

import com.gin.pixiv_manager.sys.utils.TimeUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.ZonedDateTime;


/**
 * 标准返回对象
 * @author bx002
 */
@Data
@ApiModel("标准返回对象")
public class Res<T> implements Serializable {
    @ApiModelProperty("状态码")
    int code;
    @ApiModelProperty("消息")
    String message;
    @ApiModelProperty("时间戳")
    long timestamp;
    @ApiModelProperty("时间戳的日期时间格式")
    String dateTime;
    @ApiModelProperty("额外数据")
    T data;

    public Res(int code, String message, T data) {
        ZonedDateTime now = ZonedDateTime.now();

        this.code = code;
        this.message = message;
        this.timestamp = now.toEpochSecond();
        this.dateTime = TimeUtils.DATE_TIME_FORMATTER.format(now);
        this.data = data;
    }

    public static Res<Void> success(String message) {
        return new Res<>(2000, message, null);
    }

    public static Res<Void> fail(int code, String message) {
        return fail(code, message, null);
    }

    public static <T> Res<T> fail(int code, String message, T data) {
        return new Res<>(code, message, data);
    }

    public static <T> Res<T> success(String message, T data) {
        return new Res<>(2000, message, data);
    }

}
