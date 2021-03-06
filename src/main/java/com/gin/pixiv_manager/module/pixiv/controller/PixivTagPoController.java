package com.gin.pixiv_manager.module.pixiv.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gin.pixiv_manager.module.files.service.Aria2DownloadTaskPoService;
import com.gin.pixiv_manager.module.files.service.PixivFilesService;
import com.gin.pixiv_manager.module.pixiv.bo.Filter4PixivTagPo;
import com.gin.pixiv_manager.module.pixiv.bo.PixivTagPo4Set;
import com.gin.pixiv_manager.module.pixiv.bo.TagDictionary;
import com.gin.pixiv_manager.module.pixiv.entity.PixivIllustPo;
import com.gin.pixiv_manager.module.pixiv.entity.PixivIllustTagPo;
import com.gin.pixiv_manager.module.pixiv.entity.PixivTagPo;
import com.gin.pixiv_manager.module.pixiv.service.PixivIllustTagPoService;
import com.gin.pixiv_manager.module.pixiv.service.PixivTagPoService;
import com.gin.pixiv_manager.module.pixiv.service.PixivTagTypePoService;
import com.gin.pixiv_manager.sys.request.PageParams;
import com.gin.pixiv_manager.sys.response.Res;
import com.gin.pixiv_manager.sys.utils.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static com.gin.pixiv_manager.module.pixiv.entity.PixivTagPo.TYPE_BMK_COUNT;

/**
 * PixivTag管理接口
 * @author bx002
 */
@RestController
@Slf4j
@Validated
@RequiredArgsConstructor
@RequestMapping("/pixiv/tag")
@Api(tags = PixivTagPoController.NAMESPACE + "相关接口")
@Transactional(rollbackFor = Exception.class)
public class PixivTagPoController {
    public static final String NAMESPACE = "PixivTag";

    private final PixivTagPoService service;
    private final PixivFilesService pixivFilesService;
    private final Aria2DownloadTaskPoService aria2DownloadTaskPoService;
    private final PixivIllustTagPoService pixivIllustTagPoService;
    private final PixivTagTypePoService pixivTagTypePoService;
    /**
     * 标签的范例缓存
     */
    private final Map<String, List<String>> tagExamplesCache = new HashMap<>();

    @PostMapping("findAllCompletedTags")
    @ApiOperation(value = "查询所有已完成标签")
    public Res<List<PixivTagPo>> findAllCompletedTags() {

        final QueryWrapper<PixivTagPo> qw = new QueryWrapper<>();
        TagDictionary.selectCompleted(qw);

        return Res.success("查询所有已完成标签成功", service.list(qw));
    }

    @PostMapping("findAllTypes")
    @ApiOperation(value = "查询所有标签分类")
    public Res<List<String>> findAllTypes() {
        return Res.success("查询所有标签分类成功", pixivTagTypePoService.listAllName());
    }

    @PostMapping("page")
    @ApiOperation(value = "查询分页" + NAMESPACE)
    public Res<Page<PixivTagPo>> page(@Validated @RequestBody PageParams<Filter4PixivTagPo> params) throws IOException {
        QueryWrapper<PixivTagPo> qw = new QueryWrapper<>();
//      修改查询条件
        Filter4PixivTagPo filter = params.getFilter();
        if (filter != null) {
            filter.handleQueryWrapper(qw);

            final String dirName = filter.getDirName();
            if (!StringUtils.isEmpty(dirName)) {
                final Set<Long> pid = pixivFilesService.listPidOfDir(dirName);
                final List<String> tags = pixivIllustTagPoService.listTagByPid(pid);
                qw.in("tag", tags);
            }
        }

        /*todo 暂时屏蔽收藏数tag */
        qw.and(q -> q.isNull("type").or().ne("type", TYPE_BMK_COUNT));

//        /*todo 优先查找人物+作品tag*/
//        qw.and(q->q.like("tag","(").or().like("original_translation","("));

        Page<PixivTagPo> page = service.page(new Page<>(params.getPage(), params.getSize()), qw);
//      后续处理
        List<PixivTagPo> records = page.getRecords();
        log.info("找到作品 {} 个", page.getTotal());
//        补充翻译推荐
        final TagDictionary dic = new TagDictionary(service);

        for (PixivTagPo tag : records) {
            tag.setSuggest(dic.suggestCustomTranslation(tag));
            tag.setSuggestRedirect(dic.suggestRedirect(tag));
        }

//        补充图片地址
//        持有每个tag的作品pid
        final Map<String, List<PixivIllustTagPo>> tag2PidMap =
                pixivIllustTagPoService.listPidByTag(records.stream().map(PixivTagPo::getTag).collect(Collectors.toList()))
                        .stream().collect(Collectors.groupingBy(PixivIllustTagPo::getTag));
        final List<File> allFiles = aria2DownloadTaskPoService.getAllFiles("/pixiv");
        if (allFiles.size() != 0) {
            for (PixivTagPo tag : records) {
//            检查缓存 有直接使用
                final List<String> cache = tagExamplesCache.get(tag.getTag());
                if (cache != null) {
                    tag.setExamples(cache);
                    continue;
                }
//            pid列表
                final List<Long> list = tag2PidMap.get(tag.getTag()).stream().map(PixivIllustTagPo::getPid).collect(Collectors.toList());
                if (StringUtils.isEmpty(list)) {
                    continue;
                }
                final List<String> pathList = allFiles.stream()
//                    找出文件中 pid 存在于列表中的文件
                        .filter(file -> !file.getPath().endsWith("zip"))
                        .filter(file -> {
                            final String name = file.getName();
                            final Matcher m1 = PixivIllustPo.ILLUST_FILE_NAME_PATTERN.matcher(name);
                            return m1.find() && list.contains(Long.parseLong(m1.group(1)));
                        })
                        .map(File::getPath)
                        .map(path -> path.replace("\\", "/"))
                        .map(path -> path.substring(path.indexOf("/pixiv")))
                        .sorted((o1, o2) -> o2.compareToIgnoreCase(o1))
                        .limit(5)
                        .collect(Collectors.toList());
                tagExamplesCache.put(tag.getTag(), pathList);
                tag.setExamples(pathList);
            }
        }
        return Res.success("查询" + NAMESPACE + "分页数据成功", page);
    }

    @PostMapping("setCustomTranslation")
    @ApiOperation(value = "设置自定义翻译")
    public Res<Void> setCustomTranslation(@Validated @RequestBody PixivTagPo4Set pixivTagPo4Set) {
        final String translation = pixivTagPo4Set.getTranslation();
        final String tag = pixivTagPo4Set.getTag();
        final String type = pixivTagPo4Set.getType();

        pixivTagTypePoService.validTypeName(type);

        final QueryWrapper<PixivTagPo> qw = new QueryWrapper<>();
        qw.eq("custom_translation", translation);
        final PixivTagPo pixivTagPo = service.getOne(qw);
        if (pixivTagPo != null && !pixivTagPo.getTag().equals(tag)) {
//            已经存在tag
            final String destTag = pixivTagPo.getTag();
            PixivTagPo entity = new PixivTagPo();
            entity.setTag(tag);
            entity.setRedirect(destTag);
            service.updateById(entity);
            return Res.success("设置重定向成功");
        }
        PixivTagPo entity = service.getById(tag);
        entity.setCustomTranslation(translation);
        entity.setType(type);
        service.updateById(entity);
        return Res.success("设置自定义翻译成功");
    }

    @Scheduled(cron = "0 0/10 * * * ?")
    public void clearTagExamplesCache() {
        tagExamplesCache.clear();
    }
}
