package com.example.attendance.service;

/**
 * 用户业务层 —— 处理注册和登录相关逻辑
 *
 * 【注册流程】
 * 1. AuthController 接收表单数据并验证
 * 2. 调用 UserService.register(user) 保存用户
 * 3. register() 方法内使用 BCrypt 加密密码（绝不存明文！）
 * 4. 设置默认角色和启用状态后保存到数据库
 *
 * 【为什么密码要加密？】
 * 如果数据库被拖库，明文密码会直接暴露。
 * BCrypt 是单向哈希算法，即使数据库泄露也无法还原出原始密码。
 * 每次加密结果都不一样（内部加了"盐"），同一密码两次加密结果不同。
 */

import com.example.attendance.entity.Role;
import com.example.attendance.entity.User;
import com.example.attendance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 注册新用户
     * @param user 前端提交的用户信息（密码是明文）
     */
    public void register(User user) {
        // 关键步骤：用 BCrypt 加密密码，绝对不能存明文！
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // 默认角色为学生
        if (user.getRole() == null) user.setRole(Role.STUDENT);
        user.setEnabled(true);
        userRepository.save(user);
    }

    /** 根据用户名查找用户（登录时使用） */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
}