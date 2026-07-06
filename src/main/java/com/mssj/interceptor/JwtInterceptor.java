package com.mssj.interceptor;

import com.mssj.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // 排除登录和公开的API以及静态资源
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/auth/") ||
            requestURI.startsWith("/public") ||
            requestURI.startsWith("/static") ||
            requestURI.equals("/login.html") ||
            requestURI.equals("/register.html") ||
            requestURI.equals("/user_agreement.html") ||
            requestURI.equals("/privacy_policy.html") ||
            requestURI.startsWith("/job-graph") ||
            requestURI.endsWith(".css") ||
            requestURI.endsWith(".js") ||
            requestURI.equals("/") ||
            requestURI.equals("/favicon.ico")) {
            return true;
        }

        // 获取token
        String token = getTokenFromRequest(request);

        if (token == null) {
            log.warn("请求 {} 缺少JWT token", requestURI);
            // 对于HTML页面请求，重定向到登录页面
            if (requestURI.endsWith(".html")) {
                response.sendRedirect("/login.html");
                return false;
            }
            // 对于API请求，返回JSON错误
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=UTF-8");
            // 返回JSON格式的错误信息
            String jsonError = "{\"code\":401,\"message\":\"缺少认证token\",\"data\":null}";
            response.getWriter().write(jsonError);
            return false;
        }

        // 验证token
        if (!jwtUtils.validateToken(token)) {
            log.warn("请求 {} 的JWT token无效", requestURI);
            // 对于HTML页面请求，重定向到登录页面
            if (requestURI.endsWith(".html")) {
                response.sendRedirect("/login.html");
                return false;
            }
            // 对于API请求，返回JSON错误
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=UTF-8");
            // 返回JSON格式的错误信息
            String jsonError = "{\"code\":401,\"message\":\"无效的认证token\",\"data\":null}";
            response.getWriter().write(jsonError);
            return false;
        }

        // 检查token是否过期
        if (jwtUtils.isTokenExpired(token)) {
            log.warn("请求 {} 的JWT token已过期", requestURI);
            // 对于HTML页面请求，重定向到登录页面
            if (requestURI.endsWith(".html")) {
                response.sendRedirect("/login.html");
                return false;
            }
            // 对于API请求，返回JSON错误
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=UTF-8");
            // 返回JSON格式的错误信息
            String jsonError = "{\"code\":401,\"message\":\"认证token已过期\",\"data\":null}";
            response.getWriter().write(jsonError);
            return false;
        }

        // 将用户信息添加到请求属性中，方便后续使用
        String username = jwtUtils.getUsernameFromToken(token);
        Long userId = jwtUtils.getUserIdFromToken(token);
        String role = jwtUtils.getUserRoleFromToken(token);

        request.setAttribute("currentUsername", username);
        request.setAttribute("currentUserId", userId);
        request.setAttribute("currentUserRole", role);

        log.info("用户 {} (ID: {}) 访问 {} 已通过拦截器验证",
                username, userId, requestURI);

        return true;
    }

    /**
     * 从请求中提取JWT token
     * @param request HTTP请求
     * @return JWT token，如果没有则返回null
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