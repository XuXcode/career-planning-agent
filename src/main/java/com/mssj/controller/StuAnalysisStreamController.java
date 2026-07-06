package com.mssj.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mssj.pojo.CareerReport;
import com.mssj.service.AnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

/**
 * SSE 流式分析控制器 (v2.0)
 *
 * 通过 Server-Sent Events 将 AI 分析的 4 个阶段实时推送到前端，
 * 解决传统同步 HTTP 请求在报告生成耗时较长时易超时的问题。
 *
 * 事件流:
 *   phase → "正在提取能力画像..."
 *   profile → StudentProfile JSON
 *   phase → "正在进行岗位匹配分析..."
 *   matches → JobMatchAnalysis JSON
 *   phase → "正在规划职业路径..."
 *   path → CareerPathPlan JSON
 *   phase → "正在生成行动计划..."
 *   plan → ActionPlan JSON
 *   complete → "分析完成"
 *   error → error message (仅异常时)
 */
@Slf4j
@RestController
@RequestMapping("/stu")
public class StuAnalysisStreamController {

    @Autowired
    private AnalysisService analysisService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final long SSE_TIMEOUT = 300_000L; // 5 分钟超时

    /**
     * SSE 流式职业规划分析
     * GET /stu/analysis/stream
     */
    @GetMapping(value = "/analysis/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamAnalysis(HttpServletRequest request) {
        String username = (String) request.getAttribute("currentUsername");
        if (username == null) {
            SseEmitter errorEmitter = new SseEmitter(SSE_TIMEOUT);
            sendEvent(errorEmitter, "error", "用户未认证，请先登录");
            errorEmitter.complete();
            return errorEmitter;
        }

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        // 回调注册
        emitter.onCompletion(() -> log.info("SSE 流式传输完成: 用户={}", username));
        emitter.onTimeout(() -> log.warn("SSE 流式传输超时: 用户={}", username));
        emitter.onError(e -> log.error("SSE 流式传输异常: 用户={}, {}", username, e.getMessage()));

        // 异步执行 AI 分析，不阻塞主线程
        CompletableFuture.runAsync(() -> {
            try {
                // ── 读取用户文件 ──
                String userUploadDir = uploadDir + "/" + username;
                File uploadDirectory = new File(userUploadDir);
                if (!uploadDirectory.exists() || uploadDirectory.listFiles() == null
                        || uploadDirectory.listFiles().length == 0) {
                    sendEvent(emitter, "error", "用户上传目录为空，请先上传简历文件");
                    emitter.complete();
                    return;
                }

                String content = readAllFiles(uploadDirectory);

                // ── Phase 1: 能力提取 ──
                sendEvent(emitter, "phase", "正在提取能力画像...");
                CareerReport.StudentProfile profile = analysisService.extractProfile(content);
                sendEvent(emitter, "profile", profile);
                log.info("[SSE] Phase 1 完成: 用户={}", username);

                // ── Phase 2: 岗位匹配 ──
                sendEvent(emitter, "phase", "正在进行四维岗位匹配分析...");
                CareerReport.JobMatchAnalysis matchAnalysis = analysisService.matchJobs(profile);
                sendEvent(emitter, "matches", matchAnalysis);
                log.info("[SSE] Phase 2 完成: 用户={}", username);

                // ── Phase 3: 职业路径 ──
                sendEvent(emitter, "phase", "正在规划职业发展路径...");
                CareerReport.CareerPathPlan pathPlan = analysisService.planCareerPath(profile, matchAnalysis);
                sendEvent(emitter, "path", pathPlan);
                log.info("[SSE] Phase 3 完成: 用户={}", username);

                // ── Phase 4: 行动计划 ──
                sendEvent(emitter, "phase", "正在生成行动计划...");
                CareerReport.ActionPlan actionPlan = analysisService.generateActionPlan(
                        profile, matchAnalysis, pathPlan);
                sendEvent(emitter, "plan", actionPlan);
                log.info("[SSE] Phase 4 完成: 用户={}", username);

                // ── 完成 ──
                // 组装完整报告作为最终数据
                CareerReport fullReport = new CareerReport();
                fullReport.setStudentProfile(profile);
                fullReport.setJobMatchAnalysis(matchAnalysis);
                fullReport.setCareerPathPlan(pathPlan);
                fullReport.setActionPlan(actionPlan);
                sendEvent(emitter, "complete", fullReport);

                emitter.complete();
                log.info("[SSE] 全流程完成: 用户={}", username);

            } catch (Exception e) {
                log.error("[SSE] 分析失败: 用户={}, {}", username, e.getMessage(), e);
                sendEvent(emitter, "error", "分析失败: " + e.getMessage());
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    // ─── 辅助方法 ──────────────────────────────────────────────

    private String readAllFiles(File directory) throws IOException {
        StringBuilder sb = new StringBuilder();
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    sb.append("--- 文件: ").append(file.getName()).append(" ---\n");
                    sb.append(new String(Files.readAllBytes(file.toPath()))).append("\n\n");
                }
            }
        }
        return sb.toString();
    }

    private void sendEvent(SseEmitter emitter, String eventName, Object data) {
        try {
            String json = (data instanceof String) ? (String) data : objectMapper.writeValueAsString(data);
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(json, MediaType.APPLICATION_JSON));
        } catch (IOException e) {
            log.error("SSE 事件发送失败 [{}]: {}", eventName, e.getMessage());
        }
    }
}
