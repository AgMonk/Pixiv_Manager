package com.gin.pixiv_manager.module.pixiv.entity.tasks;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsKey;
import com.gitee.sunchenbin.mybatis.actable.annotation.TableComment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 未分类作品下载、添加tag任务
 * @author bx002
 */
@Data
@TableName(value = "t_pixiv_task_untagged_illust", autoResultMap = true)
@TableComment("未分类作品下载、添加tag任务")
@NoArgsConstructor
@AllArgsConstructor
public class PixivUntaggedIllustTaskPo implements Serializable {
    @TableId
    @IsKey
    @Column(isNull = false)
    Long pid;

//    获取详情 添加tag 添加下载任务
}