package com.mssj.controller;

import com.mssj.pojo.Result;
import com.mssj.service.StudentInputService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/stu")
public class StuFileController {

    // 获取配置的文件存储路径
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private StudentInputService studentInputService;

    @PostMapping("/uploadResume")
    public Result uploadResume(@RequestParam("resume") MultipartFile file, HttpServletRequest request) {
        log.info("开始上传文件");
        if (file.isEmpty()) {
            return Result.fail("文件为空");
        }

        try {
            // 获取当前登录用户的用户名
            String username = (String) request.getAttribute("currentUsername");
            if (username == null) {
                return Result.fail("用户未认证，请先登录");
            }

            // 创建用户特定的文件夹
            String userUploadDir = uploadDir + "/" + username;

            String fileName = generateUniqueFileName(file.getOriginalFilename());
            File destination = new File(userUploadDir + "/" + fileName);

            destination.getParentFile().mkdirs();

            file.transferTo(destination);

            log.info("用户 {} 上传文件成功: {}", username, fileName);
            return Result.success("文件上传成功");
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.fail("文件上传失败：" + e.getMessage());
        }
    }

    @PostMapping("/uploadText")
    public Result uploadText(@RequestBody String studentText, HttpServletRequest request) {
        log.info("开始上传文本");
        if (studentText == null || studentText.isEmpty()) {
            return Result.fail("文本为空");
        }
        try {
            // 获取当前登录用户的用户名
            String username = (String) request.getAttribute("currentUsername");
            if (username == null) {
                return Result.fail("用户未认证，请先登录");
            }

            // 创建用户特定的文件夹
            String userUploadDir = uploadDir + "/" + username;

            String fileName = generateUniqueFileName("student_input.txt");
            File textFile = new File(userUploadDir + "/" + fileName);

            studentInputService.saveTextAsFile(studentText, textFile);

            textFile.getParentFile().mkdirs();

            log.info("用户 {} 上传文本成功: {}", username, fileName);
            return Result.success("文本上传成功！");
        } catch (IOException e) {
            log.error("文本上传失败", e);
            return Result.fail("保存文本失败：" + e.getMessage());
        }
    }
    private String generateUniqueFileName(String originalFilename) {
        String randomStr = UUID.randomUUID().toString().replaceAll("-", "");
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        return randomStr + extension;
    }
}