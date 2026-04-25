package com.mssj.mapper;

import com.mssj.pojo.User;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface UserMapper {
    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户对象
     */
    @Select("SELECT id, username, password, role FROM user WHERE username = #{username}")
    User findByUsername(@Param("username") String username);

    /**
     * 插入新用户
     * @param user 用户对象
     * @return 插入的行数
     */
    @Insert("INSERT INTO user (username, password, role) VALUES (#{username}, #{password}, #{role})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 用户对象
     */
    @Select("SELECT id, username, password, role FROM user WHERE id = #{id}")
    User findById(@Param("id") Long id);

    /**
     * 查询所有用户（用于调试）
     * @return 用户列表
     */
    @Select("SELECT id, username, password, role FROM user")
    List<User> findAll();
}