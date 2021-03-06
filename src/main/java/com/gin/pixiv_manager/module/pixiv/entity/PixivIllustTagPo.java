package com.gin.pixiv_manager.module.pixiv.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.annotation.Index;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsKey;
import com.gitee.sunchenbin.mybatis.actable.annotation.TableComment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 作品名下的tag
 * @author bx002
 */
@Data
@TableName(value = "t_pixiv_relate_illust_tag", autoResultMap = true)
@TableComment("作品名下的tag")
@NoArgsConstructor
@AllArgsConstructor
public class PixivIllustTagPo implements Serializable {
    @TableId
    @IsKey
    @Column(length = 36, isNull = false)
    String uuid;

    @Column(isNull = false)
    @Index
    Long pid;

    @Column(comment = "标签名称", isNull = false)
    @Index
    String tag;

    @Column(comment = "是否需要统计数量", defaultValue = "1")
    @Index
    Integer needCount;

    @TableField(exist = false)
    Integer count;

    public void createUuid() {
        this.uuid = UUID.randomUUID().toString();
    }

    public static List<PixivIllustTagPo> parse(long pid, List<String> tags) {
        return tags.stream().map(i -> {
            final PixivIllustTagPo entity = new PixivIllustTagPo();
            entity.createUuid();
            entity.setPid(pid);
            entity.setTag(i);
            return entity;
        }).collect(Collectors.toList());
    }

    public PixivTagPo toPixivTagPo() {
        final PixivTagPo po = new PixivTagPo();
        po.setTag(this.tag);
        po.setCount(this.count);
        return po;
    }
}