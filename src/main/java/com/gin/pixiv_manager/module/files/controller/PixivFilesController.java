package com.gin.pixiv_manager.module.files.controller;

import com.gin.pixiv_manager.module.files.service.PixivFilesService;
import com.gin.pixiv_manager.sys.response.Res;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * pixiv文件接口管理接口
 * @author bx002
 */
@RestController
@Slf4j
@Validated
@RequiredArgsConstructor
@RequestMapping("/files/pixiv")
@Api(tags = PixivFilesController.NAMESPACE + "相关接口")
@Transactional(rollbackFor = Exception.class)
public class PixivFilesController {
    public static final String NAMESPACE = "pixiv文件接口";

    private final PixivFilesService service;

    @PostMapping("listDirs")
    @ApiOperation(value = "查询未分类作品文件夹")
    public Res<List<String>> listDirs() {
        return Res.success("查询未分类作品文件夹成功", service.listDirs());
    }

    @PostMapping("arrangeDir")
    @ApiOperation(value = "归档一个未分类作品文件夹")
    public Res<Void> arrangeDir(String dirName) throws IOException {
        service.arrangeFiles(dirName);
        return Res.success("归档一个未分类作品文件夹成功");
    }
}
