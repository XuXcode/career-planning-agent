package com.mssj.controller;

import com.mssj.pojo.Result;
import com.mssj.service.StudentInputService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
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

    // ═══════════════════════════════════════════════════════════
    // 企业级文件导出端点 (v2.0)
    // 基于 poi-tl + iText7，当前为结构桩（返回 501）
    // ═══════════════════════════════════════════════════════════

    /**
     * 导出职业规划报告为 PDF
     * GET /stu/export/report/pdf
     *
     * TODO: 使用 iText7 实现 PDF 生成
     * 1. 从数据库/缓存获取用户最新的 CareerReport
     * 2. 使用 iText7 构建 PDF 文档结构
     * 3. 返回 application/pdf 下载流
     */
    @GetMapping("/export/report/pdf")
    public ResponseEntity<byte[]> exportReportPdf(HttpServletRequest request) {
        String username = (String) request.getAttribute("currentUsername");
        if (username == null) {
            return ResponseEntity.status(401).build();
        }

        log.info("用户 {} 请求导出 PDF 报告 (功能开发中)", username);

        // STUB: 返回 501 Not Implemented
        // 完整实现需:
        //   PdfWriter writer = new PdfWriter(byteArrayOutputStream);
        //   PdfDocument pdf = new PdfDocument(writer);
        //   Document document = new Document(pdf);
        //   ... 构建 PDF 内容 ...
        //   byte[] pdfBytes = byteArrayOutputStream.toByteArray();
        //   return ResponseEntity.ok()
        //       .contentType(MediaType.APPLICATION_PDF)
        //       .header("Content-Disposition", "attachment; filename=\"career_report.pdf\"")
        //       .body(pdfBytes);

        return ResponseEntity.status(501)
                .header("X-Export-Status", "not-implemented")
                .body("PDF 导出功能开发中，敬请期待。".getBytes());
    }

    /**
     * 导出职业规划报告为 Word (DOCX)
     * GET /stu/export/report/word
     *
     * TODO: 使用 poi-tl 模板引擎实现 DOCX 生成
     * 1. 准备 Word 模板文件 (resources/templates/career_report_template.docx)
     * 2. 从数据库/缓存获取用户最新的 CareerReport
     * 3. 使用 poi-tl 渲染模板
     * 4. 返回 application/vnd.openxmlformats-officedocument.wordprocessingml.document 下载流
     */
    @GetMapping("/export/report/word")
    public ResponseEntity<byte[]> exportReportWord(HttpServletRequest request) {
        String username = (String) request.getAttribute("currentUsername");
        if (username == null) {
            return ResponseEntity.status(401).build();
        }

        log.info("用户 {} 请求导出 Word 报告 (功能开发中)", username);

        // STUB: 返回 501 Not Implemented
        // 完整实现需:
        //   XWPFTemplate template = XWPFTemplate.compile("templates/career_report_template.docx");
        //   Map<String, Object> data = buildReportData(careerReport);
        //   template.render(data);
        //   ByteArrayOutputStream out = new ByteArrayOutputStream();
        //   template.writeAndClose(out);
        //   byte[] docxBytes = out.toByteArray();
        //   return ResponseEntity.ok()
        //       .contentType(MediaType.parseMediaType(
        //           "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
        //       .header("Content-Disposition", "attachment; filename=\"career_report.docx\"")
        //       .body(docxBytes);

        return ResponseEntity.status(501)
                .header("X-Export-Status", "not-implemented")
                .body("Word 导出功能开发中，敬请期待。".getBytes());
    }
}