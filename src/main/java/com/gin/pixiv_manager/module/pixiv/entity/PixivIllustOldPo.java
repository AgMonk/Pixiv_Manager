package com.gin.pixiv_manager.module.pixiv.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.entity.PixivIllust;
import com.gin.pixiv_manager.sys.type_handler.ListStringTypeHandler;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsKey;
import com.gitee.sunchenbin.mybatis.actable.annotation.TableComment;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 作品详情旧数据
 * @author bx002
 */
@Data
@TableName(value = "t_pixiv_entity_illust_old_0", autoResultMap = true)
@TableComment("作品详情旧数据")
@NoArgsConstructor
@AllArgsConstructor
public class PixivIllustOldPo implements Serializable {
    @TableId
    @IsKey
    @Column(isNull = false)
    Long id;

    @Column(comment = "用户id", isNull = false, name = "userId")
    @TableField("userId")
    Long userId;

    @Column(comment = "标题")
    String title;

    @Column(comment = "用户名", name = "userName")
    @TableField("userName")
    String userName;

    @Column(comment = "tag", length = 2000, type = MySqlTypeConstant.VARCHAR)
    @TableField(typeHandler = ListStringTypeHandler.class)
    List<String> tag;

    @Column(comment = "tag", length = 2000, name = "tagTranslated", type = MySqlTypeConstant.VARCHAR)
    @TableField(value = "tagTranslated", typeHandler = ListStringTypeHandler.class)
    List<String> tagTranslated;

    @Column(comment = "作品类型", name = "illustType")
    @TableField("illustType")
    Integer illustType;

    @Column(comment = "收藏数", name = "bookmarkCount")
    @TableField("bookmarkCount")
    Integer bookmarkCount;

    public PixivIllustPo toPixivIllustPo() {
        final PixivIllustPo res = new PixivIllustPo();
        BeanUtils.copyProperties(this, res);

        res.setDataUpdatedTime(ZonedDateTime.now().toEpochSecond());

        switch (this.illustType) {
            case PixivIllust.ILLUST_TYPE_ILLUSTRATION -> res.setType(PixivIllustPo.ILLUST_TYPE_ILLUSTRATION);
            case PixivIllust.ILLUST_TYPE_MANGA -> res.setType(PixivIllustPo.ILLUST_TYPE_MANGA);
            case PixivIllust.ILLUST_TYPE_GIF -> res.setType(PixivIllustPo.ILLUST_TYPE_GIF);
            default -> throw new IllegalStateException("Unexpected value: " + this.illustType);
        }
        return res;
    }

    public List<PixivTagPo> toPixivTagPoList() {
        final ArrayList<PixivTagPo> list = new ArrayList<>();
        for (int i = 0; i < tag.size(); i++) {
            list.add(new PixivTagPo(tag.get(i), tagTranslated.get(i)));
        }
        return list;
    }
}