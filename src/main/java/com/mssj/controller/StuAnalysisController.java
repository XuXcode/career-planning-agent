package com.mssj.controller;


import com.mssj.pojo.Result;
import com.mssj.service.AnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.IOException;


@RestController
@RequestMapping("/stu")
@Slf4j
public class StuAnalysisController {

    @Autowired
    private AnalysisService analysisService;

    @Value("${file.upload-dir}")   // 从配置中读取
    private String uploadDir;

    @PostMapping("/analysis")
    public Result analysisFile(HttpServletRequest request) {
        try {
            // 获取当前登录用户的用户名
            String username = (String) request.getAttribute("currentUsername");
            if (username == null) {
                return Result.fail("用户未认证，请先登录");
            }

            // 使用用户特定的文件夹
            String userUploadDir = uploadDir + "/" + username;
            log.info("大模型开始获取文件，用户目录：{}", userUploadDir);

            File uploadDirectory = new File(userUploadDir);
            // 如果目录不存在则创建
            if (!uploadDirectory.exists()) {
                uploadDirectory.mkdirs();
                return Result.fail("用户上传目录不存在，已创建，请先上传文件");
            }

            // 检查用户目录是否为空
            File[] files = uploadDirectory.listFiles();
            if (files == null || files.length == 0) {
                return Result.fail("用户上传目录为空，请先上传文件");
            }

            log.info("用户 {} 开始分析文件，共 {} 个文件", username, files.length);
            return Result.success("大模型获取文件成功！",
                    analysisService.processFileWithModel(uploadDirectory));
        } catch (IOException e) {
            log.error("文件处理失败", e);
            return Result.fail("大模型获取文件失败：" + e.getMessage());
        }
    }
}

