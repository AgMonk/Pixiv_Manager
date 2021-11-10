package com.gin.pixiv_manager.module.pixiv.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gin.pixiv_manager.module.pixiv.entity.PixivCookie;
import com.gin.pixiv_manager.module.pixiv.service.PixivCookieService;
import com.gin.pixiv_manager.sys.params_validation.annotation.NotEmpty;
import com.gin.pixiv_manager.sys.request.PageParams;
import com.gin.pixiv_manager.sys.response.Res;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * PixivCookie管理接口
 * @author bx002
 */
@RestController
@Slf4j
@Validated
@RequiredArgsConstructor
@RequestMapping("PixivCookie")
@Api(tags = PixivCookieController.NAMESPACE + "相关接口")
@Transactional(rollbackFor = Exception.class)
public class PixivCookieController {
    public static final String NAMESPACE = "PixivCookie";

    private final PixivCookieService service;

    @PostMapping("add")
    @ApiOperation(value = "添加" + NAMESPACE)
    public Res<Void> add(@RequestBody @Validated PixivCookie entity) {
        service.save(entity);
        return Res.success("添加成功");
    }

    @PostMapping("update")
    @ApiOperation(value = "修改" + NAMESPACE)
    public Res<Void> update(@RequestBody @Validated PixivCookie entity) {
        service.updateById(entity);
        return Res.success("修改成功");
    }

    @PostMapping("del")
    @ApiOperation(value = "删除" + NAMESPACE)
    public Res<Void> del(@NotEmpty("uuid") @RequestParam String uuid) {
        if (service.getById(uuid) == null) {
            return Res.fail(4000, "uuid错误");
        }
        service.removeById(uuid);
        return Res.success("删除成功");
    }

    @PostMapping("findAll")
    @ApiOperation(value = "查询全部" + NAMESPACE)
    public Res<List<PixivCookie>> findAll() {
        QueryWrapper<PixivCookie> qw = new QueryWrapper<>();

        return Res.success("查询" + NAMESPACE + "全部数据成功", service.list(qw));
    }

    @PostMapping("get")
    @ApiOperation(value = "查询单个" + NAMESPACE + "详情")
    public Res<PixivCookie> get(@NotEmpty("uuid") @RequestParam String uuid) {
        PixivCookie entity = service.getById(uuid);
        // 补充信息

        return Res.success("查询成功", entity);
    }

//    @PostMapping("page")
//    @ApiOperation(value = "查询分页" + NAMESPACE)
//    public Res<Page<PixivCookie>> page(@RequestBody PageParams<PixivCookie> params) {
//        QueryWrapper<PixivCookie> qw = new QueryWrapper<>();
////      修改查询条件
//        PixivCookie filter = params.getFilter();
//        if (filter != null) {
//            filter.handleQueryWrapper(qw);
//        }
//
//        Page<PixivCookie> page = service.page(new Page<>(params.getPage(), params.getSize()), qw);
////      后续处理
//        List<PixivCookie> records = page.getRecords();
//
//        return Res.success("查询" + NAMESPACE + "分页数据成功", page);
//    }
}
