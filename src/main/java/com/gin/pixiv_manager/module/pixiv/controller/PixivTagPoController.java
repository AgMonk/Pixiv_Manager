package com.gin.pixiv_manager.module.pixiv.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gin.pixiv_manager.module.pixiv.bo.Filter4PixivTagPo;
import com.gin.pixiv_manager.module.pixiv.bo.PixivTagPo4Set;
import com.gin.pixiv_manager.module.pixiv.bo.TagDictionary;
import com.gin.pixiv_manager.module.pixiv.entity.PixivTagPo;
import com.gin.pixiv_manager.module.pixiv.service.PixivTagPoService;
import com.gin.pixiv_manager.sys.request.PageParams;
import com.gin.pixiv_manager.sys.response.Res;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
        return Res.success("查询所有标签分类成功", PixivTagPo.TYPES);
    }

    @PostMapping("page")
    @ApiOperation(value = "查询分页" + NAMESPACE)
    public Res<Page<PixivTagPo>> page(@Validated @RequestBody PageParams<Filter4PixivTagPo> params) {
        QueryWrapper<PixivTagPo> qw = new QueryWrapper<>();
//      修改查询条件
        Filter4PixivTagPo filter = params.getFilter();
        if (filter != null) {
            filter.handleQueryWrapper(qw);
        }

        Page<PixivTagPo> page = service.page(new Page<>(params.getPage(), params.getSize()), qw);
//      后续处理
        List<PixivTagPo> records = page.getRecords();

        return Res.success("查询" + NAMESPACE + "分页数据成功", page);
    }

    @PostMapping("setCustomTranslation")
    @ApiOperation(value = "设置自定义翻译")
    public Res<Void> setCustomTranslation(@Validated @RequestBody PixivTagPo4Set pixivTagPo4Set) {
        final String translation = pixivTagPo4Set.getTranslation();
        final String tag = pixivTagPo4Set.getTag();

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
        entity.setType(pixivTagPo4Set.getType());
        service.updateById(entity);
        return Res.success("设置自定义翻译成功");
    }
}
