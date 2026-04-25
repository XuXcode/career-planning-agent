package com.mssj.controller;

import com.mssj.pojo.Result;
import com.mssj.pojo.User;
import com.mssj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
public class RegisterController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     * @param username 用户名
     * @param password 密码
     * @param confirmPassword 确认密码
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result register(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String confirmPassword
    ) {
        log.info("用户注册请求: username = {}", username);

        // 参数验证
        if (username == null || username.trim().isEmpty()) {
            return Result.fail("用户名不能为空");
        }

        if (password == null || password.trim().isEmpty()) {
            return Result.fail("密码不能为空");
        }

        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            return Result.fail("确认密码不能为空");
        }

        if (username.trim().length() < 3) {
            return Result.fail("用户名长度不能少于3个字符");
        }

        if (password.length() < 6) {
            return Result.fail("密码长度不能少于6个字符");
        }

        if (!password.equals(confirmPassword)) {
            return Result.fail("两次输入的密码不一致");
        }

        // 检查用户名是否已存在
        if (userService.existsByUsername(username.trim())) {
            return Result.fail("用户名已存在");
        }

        // 创建用户对象
        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(password);
        user.setRole("user"); // 默认角色

        // 注册用户
        Long userId = userService.register(user);
        if (userId != null) {
            log.info("用户 {} 注册成功，用户ID: {}", username, userId);

            // 创建返回数据（不包含密码）
            Map<String, Object> data = new HashMap<>();
            data.put("userId", userId);
            data.put("username", user.getUsername());
            data.put("role", user.getRole());

            return Result.success("注册成功", data);
        } else {
            log.warn("用户 {} 注册失败", username);
            return Result.fail("注册失败，请稍后重试");
        }
    }

    /**
     * 检查用户名是否可用
     * @param username 用户名
     * @return 检查结果
     */
    @GetMapping("/check-username")
    public Result checkUsername(@RequestParam String username) {
        if (username == null || username.trim().isEmpty()) {
            return Result.fail("用户名不能为空");
        }

        boolean exists = userService.existsByUsername(username.trim());

        if (exists) {
            // 如果已存在，直接返回失败状态码（比如 400 或 500）
            // 这样前端可以直接通过 code 判断，不用去猜字符串内容
            return Result.fail("用户名已存在");
        } else {
            // 如果不存在，返回成功
            return Result.success("用户名可用");
        }
    }
}