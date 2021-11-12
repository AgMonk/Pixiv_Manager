package com.gin.pixiv_manager.module.aria2.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gin.pixiv_manager.module.aria2.utils.request.Aria2Request;
import com.gin.pixiv_manager.module.aria2.utils.request.Aria2UriOption;
import com.gin.pixiv_manager.sys.type_handler.ListStringTypeHandler;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsKey;
import com.gitee.sunchenbin.mybatis.actable.annotation.TableComment;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Aria2下载任务
 * @author bx002
 */
@Data
@TableName(value = "t_aria2_entity_download_task", autoResultMap = true)
@TableComment("Aria2下载任务")
@NoArgsConstructor
@AllArgsConstructor
public class Aria2DownloadTaskPo implements Serializable {
    @TableId
    @IsKey
    @Column(length = 36, isNull = false)
    String uuid;

    @Column(comment = "gid", length = 60)
    String gid;

    @Column(comment = "urls", length = 2000, isNull = false, type = MySqlTypeConstant.VARCHAR)
    @TableField(typeHandler = ListStringTypeHandler.class)
    List<String> urls = new ArrayList<>();

    @Column(comment = "存放目录", isNull = false)
    String dir;

    @Column(comment = "文件名", isNull = false)
    String fileName;

    @Column(comment = "类型", isNull = false)
    String type;

    @Column(comment = "优先级", isNull = false)
    Integer priority;

    @Column(comment = "创建时间", isNull = false)
    Long timestamp;

    public void addUrl(String url) {
        urls.add(url);
    }

    public void createUuid() {
        this.uuid = UUID.randomUUID().toString();
        this.timestamp = ZonedDateTime.now().toEpochSecond();
    }

    public Aria2UriOption toOption() {
        final Aria2UriOption option = new Aria2UriOption();
        option.setDir(dir);
        option.setFileName(fileName);
        return option;
    }

    public Aria2DownloadTaskPo execute() {
        this.gid = Aria2Request.addUri(this.urls, this.toOption()).getResult();
        Aria2DownloadTaskPo entity = new Aria2DownloadTaskPo();
        entity.setUuid(this.uuid);
        entity.setUrls(this.urls);
        entity.setGid(this.gid);
        return entity;
    }
}