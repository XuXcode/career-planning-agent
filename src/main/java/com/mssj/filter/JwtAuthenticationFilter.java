package com.mssj.filter;

import com.mssj.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 跳过公开访问的路径
        String requestURI = request.getRequestURI();
        if (shouldSkipAuthentication(requestURI)) {
            log.info("Skipping JWT authentication for public path: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = getTokenFromRequest(request);
            log.info("请求 {} - token存在: {}, token: {}", request.getRequestURI(), token != null,
                     token != null ? token.substring(0, Math.min(token.length(), 20)) + "..." : "null");

            if (token != null && jwtUtils.validateToken(token)) {
                String username = JwtUtils.getUsernameFromToken(token);
                Long userId = JwtUtils.getUserIdFromToken(token);
                String role = JwtUtils.getUserRoleFromToken(token);

                if (username != null) {
                    // 将用户信息添加到请求属性中，方便后续使用
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("username", username);
                    userInfo.put("userId", userId);
                    userInfo.put("role", role);

                    request.setAttribute("userInfo", userInfo);
                    request.setAttribute("currentUsername", username);
                    request.setAttribute("currentUserId", userId);
                    request.setAttribute("currentUserRole", role);

                    log.info("用户 {} (ID: {}) 已通过JWT认证", username, userId);
                }
            } else {
                if (token != null) {
                    log.warn("JWT token无效或已过期: {}", token.substring(0, Math.min(token.length(), 30)) + "...");
                } else {
                    log.warn("请求 {} 缺少JWT token", request.getRequestURI());
                }

                // 对于需要认证的请求但token无效的情况，返回401
                if (!isPublicPath(requestURI)) {
                    sendUnauthorizedResponse(response, requestURI);
                    return;
                }
            }
        } catch (Exception e) {
            log.error("JWT认证处理失败: {}", e.getMessage());

            // 对于需要认证的请求但出现异常的情况，返回401
            if (!isPublicPath(requestURI)) {
                sendUnauthorizedResponse(response, requestURI);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 判断是否应该跳过认证
     */
    private boolean shouldSkipAuthentication(String requestURI) {
        return requestURI.startsWith("/auth/") ||
               requestURI.equals("/login.html") ||
               requestURI.equals("/register.html") ||
               requestURI.equals("/index.html") ||
               requestURI.equals("/user_agreement.html") ||
               requestURI.equals("/privacy_policy.html") ||
               requestURI.startsWith("/static/") ||
               requestURI.endsWith(".css") ||
               requestURI.endsWith(".js") ||
               requestURI.equals("/") ||
               requestURI.equals("/favicon.ico") ||
               requestURI.startsWith("/job-profile") ||
               requestURI.startsWith("/job-relation") ||
               requestURI.startsWith("/job-graph") ||
               requestURI.startsWith("/hybridaction/") ||
               requestURI.startsWith("/api/auth/"); // 允许API认证相关路径
    }

    /**
     * 判断是否为公开路径
     */
    private boolean isPublicPath(String requestURI) {
        return shouldSkipAuthentication(requestURI);
    }

    /**
     * 发送未授权响应
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String requestURI) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        String jsonError = "{\"code\":401,\"message\":\"认证失败，请重新登录\",\"data\":null}";
        response.getWriter().write(jsonError);
    }

    /**
     * 从请求中提取JWT token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        // 尝试从查询参数获取token
        String tokenParam = request.getParameter("token");
        if (tokenParam != null && !tokenParam.trim().isEmpty()) {
            return tokenParam.trim();
        }
        return null;
    }
}