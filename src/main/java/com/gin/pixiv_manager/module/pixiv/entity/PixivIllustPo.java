package com.gin.pixiv_manager.module.pixiv.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.body.PixivIllustDetail;
import com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.entity.PixivIllust;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsKey;
import com.gitee.sunchenbin.mybatis.actable.annotation.TableComment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.io.File;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 作品详情PO
 * @author bx002
 */
@Data
@TableName(value = "t_pixiv_entity_illust", autoResultMap = true)
@TableComment("作品详情PO")
@NoArgsConstructor
@AllArgsConstructor
public class PixivIllustPo implements Serializable {
    public final static String ILLUST_TYPE_ILLUSTRATION = "插画";
    public final static String ILLUST_TYPE_MANGA = "漫画";
    public final static String ILLUST_TYPE_GIF = "动图";
    public final static Pattern ILLUST_FILE_NAME_PATTERN = Pattern.compile("(\\d+)_p(\\d+)");
    public final static Pattern ILLUST_GIF_FILE_NAME_PATTERN = Pattern.compile("(\\d+)_ugoira1920x1080");

    @TableId
    @IsKey
    @Column(isNull = false)
    Long id;
    @Column(comment = "喜欢数量", isNull = false)
    Integer likeCount;
    @Column(comment = "高", isNull = false)
    Integer height;
    @Column(comment = "宽", isNull = false)
    Integer width;
    @Column(comment = "收藏数", isNull = false)
    Integer bookmarkCount;
    @Column(comment = "收藏ID")
    Long bookmarkId;
    @Column(comment = "标题", isNull = false)
    String title;
    @Column(comment = "原图地址", isNull = false)
    String originalUrl;
    @Column(comment = "作品类型", isNull = false)
    String type;
    @Column(comment = "总页数", isNull = false)
    Integer pageCount;
    @Column(comment = "作者id", isNull = false)
    Long userId;
    @Column(comment = "创建时间")
    Long createTime;
    @Column(comment = "上传时间")
    Long uploadTime;
    @Column(comment = "本数据更新时间", isNull = false)
    Long dataUpdatedTime;

    public PixivIllustPo(PixivIllustDetail detail, Long dataUpdatedTime) {
        BeanUtils.copyProperties(detail, this);
        this.dataUpdatedTime = dataUpdatedTime != null ? dataUpdatedTime : ZonedDateTime.now().toEpochSecond();

//      处理剩余字段 bookmarkId originalUrl type createTime uploadTime
        this.bookmarkId = detail.getBookmarkData() != null ? detail.getBookmarkData().getId() : null;
        this.createTime = detail.getCreateDate() != null ? detail.getCreateDate().toEpochSecond() : null;
        this.uploadTime = detail.getUploadDate() != null ? detail.getUploadDate().toEpochSecond() : null;

        this.originalUrl = detail.getUrls().getOriginal();

        //noinspection EnhancedSwitchMigration
        switch (detail.getIllustType()) {
            case PixivIllust.ILLUST_TYPE_ILLUSTRATION:
                this.type = ILLUST_TYPE_ILLUSTRATION;
                break;
            case PixivIllust.ILLUST_TYPE_MANGA:
                this.type = ILLUST_TYPE_MANGA;
                break;
//                替换动图的地址为压缩包地址
            case PixivIllust.ILLUST_TYPE_GIF:
                this.type = ILLUST_TYPE_GIF;
                this.originalUrl = this.originalUrl
                        .substring(0, this.originalUrl.lastIndexOf("_"))
                        .replace("img-original", "img-zip-ugoira");
                this.originalUrl += "_ugoira1920x1080.zip";
                break;
        }


    }

    public static Map<Long, List<File>> groupFileByPid(List<File> files) {
        return files.stream().collect(Collectors.groupingBy(file -> {
            final String name = file.getName();

            final Matcher m1 = ILLUST_FILE_NAME_PATTERN.matcher(name);
            if (m1.find()) {
                return Long.parseLong(m1.group(1));
            }
            final Matcher m2 = ILLUST_GIF_FILE_NAME_PATTERN.matcher(name);
            if (m2.find()) {
                return Long.parseLong(m2.group(1));
            }
            return 0L;

        }));
    }

    public static String parseOriginalName(String filename) {
        final Matcher m1 = ILLUST_FILE_NAME_PATTERN.matcher(filename);
        if (m1.find()) {
            final String suffix = filename.substring(filename.lastIndexOf("."));
            return m1.group(1) + "_p" + m1.group(2) + suffix;
        }
        final Matcher m2 = ILLUST_GIF_FILE_NAME_PATTERN.matcher(filename);
        if (m2.find()) {
            return m2.group(1) + "_ugoira1920x1080.zip";
        }
        return filename;
    }
}