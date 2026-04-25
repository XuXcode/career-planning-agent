package com.mssj.service.impl;

import com.mssj.mapper.UserMapper;
import com.mssj.pojo.User;
import com.mssj.service.UserService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public Long register(User user) {
        log.info("开始用户注册: {}", user.getUsername());
        // 检查用户名是否已存在
        if (existsByUsername(user.getUsername())) {
            log.warn("用户名已存在: {}", user.getUsername());
            return null;
        }

        // 设置默认角色
        if (user.getRole() == null || user.getRole().trim().isEmpty()) {
            user.setRole("user");
        }

        // 插入用户
        int result = userMapper.insert(user);
        log.info("插入用户结果: {} 行受影响, 生成的ID: {}", result, user.getId());
        if (result > 0) {
            return user.getId();
        }
        return null;
    }

    @Override
    public User findByUsername(String username) {
        log.info("根据用户名查询用户: {}", username);
        User user = userMapper.findByUsername(username);
        log.info("查询结果用户对象: {}", user);
        return user;
    }

    @Override
    public boolean existsByUsername(String username) {
        log.info("检查用户名是否存在: {}", username);
        User user = userMapper.findByUsername(username);
        log.info("查询结果: {}", user);
        return user != null;
    }

    @PostConstruct
    public void init() {
        try {
            List<User> users = userMapper.findAll();
            log.info("数据库中共有 {} 个用户: {}", users.size(), users);
        } catch (Exception e) {
            log.error("查询用户列表失败", e);
        }
    }
}