package com.mssj.service;

import com.mssj.pojo.User;

public interface UserService {
    /**
     * 用户注册
     * @param user 用户对象
     * @return 注册结果，成功返回用户ID，失败返回null
     */
    Long register(User user);

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户对象，不存在返回null
     */
    User findByUsername(String username);

    /**
     * 检查用户名是否已存在
     * @param username 用户名
     * @return 存在返回true，不存在返回false
     */
    boolean existsByUsername(String username);
}