package com.example.attendance.service;

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
    private PasswordEncoder passwordEncoder; // 注入加密器

    public void register(User user) {
        // 关键：保存前必须加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // 确保 enabled 为 true，根据你的业务设定角色
        if (user.getRole() == null) user.setRole(Role.STUDENT);
        user.setEnabled(true);
        userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
}