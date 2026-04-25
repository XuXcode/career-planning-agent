package com.mssj;

import com.mssj.mapper.UserMapper;
import com.mssj.pojo.User;
import com.mssj.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserRegistrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Test
    public void testUserRegistration() {
        // 测试用户注册
        User user = new User();
        user.setUsername("testuser_" + System.currentTimeMillis());
        user.setPassword("123456");
        user.setRole("user");

        Long userId = userService.register(user);
        assertNotNull(userId, "用户注册应该成功");
        assertTrue(userId > 0, "用户ID应该大于0");
    }

    @Test
    public void testUsernameUniqueness() {
        // 测试用户名唯一性
        String uniqueUsername = "uniqueuser_" + System.currentTimeMillis();

        User user1 = new User();
        user1.setUsername(uniqueUsername);
        user1.setPassword("password123");

        Long userId1 = userService.register(user1);
        assertNotNull(userId1, "第一次注册应该成功");

        // 尝试用相同用户名再次注册
        User user2 = new User();
        user2.setUsername(uniqueUsername);
        user2.setPassword("password456");

        Long userId2 = userService.register(user2);
        assertNull(userId2, "重复用户名注册应该失败");
    }

    @Test
    public void testFindByUsername() {
        // 测试根据用户名查找用户
        String testUsername = "finduser_" + System.currentTimeMillis();

        User user = new User();
        user.setUsername(testUsername);
        user.setPassword("password123");

        Long userId = userService.register(user);
        assertNotNull(userId);

        User foundUser = userService.findByUsername(testUsername);
        assertNotNull(foundUser, "应该能找到已注册的用户");
        assertEquals(testUsername, foundUser.getUsername());
    }

    @Test
    public void testUsernameExists() {
        // 测试用户名存在性检查
        String testUsername = "existsuser_" + System.currentTimeMillis();

        User user = new User();
        user.setUsername(testUsername);
        user.setPassword("password123");

        userService.register(user);

        assertTrue(userService.existsByUsername(testUsername), "已存在的用户名应该返回true");
        assertFalse(userService.existsByUsername("nonexistsuser"), "不存在的用户名应该返回false");
    }
}