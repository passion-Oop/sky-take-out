package com.sky.controller.admin;

import com.aliyun.oss.internal.OSSUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sky.result.Result;
import com.sky.utils.AliOssUtil;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/admin/common")
@Tag(name = "通用接口", description = "包含通用的文件上传接口")
public class CommonController {

    @Autowired
    AliOssUtil aliOssUtil;

    @PostMapping("/upload")
    @Operation(summary = "文件上传接口")
    public Result<String> upload(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        log.info("文件上传:{}", multipartFile);
        String originalFilename = multipartFile.getOriginalFilename();
        String substring = null;

        if (originalFilename != null) {
            substring = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String objectName = UUID.randomUUID() + substring;
        String filePath = aliOssUtil.upload(multipartFile.getBytes(), objectName);
        return Result.success(filePath);
    }
}
