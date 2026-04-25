package com.mssj.controller;

import com.mssj.pojo.Result;
import com.mssj.pojo.User;
import com.mssj.service.UserService;
import com.mssj.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
public class LoginController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 登录结果，包含JWT token
     */
    @PostMapping("/login")
    public Result login(@RequestParam String username, @RequestParam String password) {
        log.info("用户登录请求: username = {}", username);

        if (username == null || username.trim().isEmpty()) {
            return Result.fail("用户名不能为空");
        }

        if (password == null || password.trim().isEmpty()) {
            return Result.fail("密码不能为空");
        }

        // 验证用户名和密码
        User user = userService.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
                // 生成JWT token
                String token = JwtUtils.generateToken(
                        user.getUsername(),
                        user.getId(),
                        user.getRole()
                );

                // 创建返回的用户信息（不包含密码）
                User responseUser = new User();
                responseUser.setId(user.getId());
                responseUser.setUsername(user.getUsername());
                responseUser.setRole(user.getRole());

                // 创建响应数据
                Map<String, Object> data = new HashMap<>();
                data.put("token", token);
                data.put("user", responseUser);

                log.info("用户 {} 登录成功", username);
                return Result.success("登录成功", data);
            }

        log.warn("用户 {} 登录失败：用户名或密码错误", username);
        return Result.fail("用户名或密码错误");
    }

    /**
     * 用户登出
     * @return 登出结果
     */
    @PostMapping("/logout")
    public Result logout() {
        log.info("用户登出");
        return Result.success("登出成功");
    }

    /**
     * 刷新JWT token
     * @param token 原token
     * @return 新的token
     */
    @PostMapping("/refresh")
    public Result refreshToken(@RequestParam String token) {
        log.info("刷新token请求");

        if (token == null || token.trim().isEmpty()) {
            return Result.fail("token不能为空");
        }

        if (!JwtUtils.validateToken(token)) {
            return Result.fail("无效的token");
        }

        String newToken = JwtUtils.refreshToken(token);
        if (newToken != null) {
            Map<String, String> data = new HashMap<>();
            data.put("token", newToken);
            return Result.success("token刷新成功", data);
        } else {
            return Result.fail("token刷新失败");
        }
    }

    /**
     * 获取当前用户信息
     * @return 用户信息
     */
    @GetMapping("/info")
    public Result getUserInfo(HttpServletRequest request) {
        log.info("获取用户信息");

        try {
            // 从Authorization header获取token
            String token = extractTokenFromRequest(request);

            if (token != null && JwtUtils.validateToken(token)) {
                String username = JwtUtils.getUsernameFromToken(token);
                Long userId = JwtUtils.getUserIdFromToken(token);
                String role = JwtUtils.getUserRoleFromToken(token);

                User user = new User();
                user.setId(userId);
                user.setUsername(username);
                user.setRole(role);

                return Result.success(user);
            }
        } catch (Exception e) {
            log.error("获取用户信息失败: {}", e.getMessage());
        }

        return Result.fail("获取用户信息失败");
    }

    /**
     * 从请求中提取JWT token
     * @param request HTTP请求
     * @return JWT token，如果没有则返回null
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        // 回退到查询参数
        String tokenParam = request.getParameter("token");
        if (tokenParam != null && !tokenParam.trim().isEmpty()) {
            return tokenParam.trim();
        }
        return null;
    }
    /**
     * 获取当前登录用户名
     * @param request HTTP请求
     * @return 包含用户名的Result
     */
    @GetMapping("/username")
    public Result getUsername(HttpServletRequest request) {
        log.info("获取当前用户名请求");
        try {
            String token = extractTokenFromRequest(request);
            if (token != null && JwtUtils.validateToken(token)) {
                String username = JwtUtils.getUsernameFromToken(token);
                return Result.success(username);
            }
        } catch (Exception e) {
            log.error("获取用户名失败: {}", e.getMessage());
        }
        return Result.fail("获取用户名失败或未登录");
    }
}