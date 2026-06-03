package com.example.attendance.controller;

/**
 * 用户认证控制器 —— 处理注册请求
 *
 * 【关于登录】
 * 登录（POST /doLogin）不由这个控制器处理，
 * 而是由 Spring Security 自动处理的（见 SecurityConfig）。
 * 这个控制器只负责"注册"功能。
 *
 * 【POST 注册流程】
 * 1. register.html 表单提交 → POST /register
 * 2. 这个控制器接收表单参数并做校验
 * 3. 调用 UserService.register() 加密密码并存入数据库
 * 4. 返回 login 页面，提示"注册成功，请登录"
 *
 * 【@RequestParam】
 * 用于获取表单提交的单个字段值。
 * 参数名默认和表单 input 的 name 属性一致，
 * 若不一致可以用 @RequestParam("表单name") 指定别名。
 */

import com.example.attendance.entity.Role;
import com.example.attendance.entity.Student;
import com.example.attendance.entity.User;
import com.example.attendance.service.StudentService;
import com.example.attendance.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private StudentService studentService;

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           @RequestParam(required = false, defaultValue = "STUDENT") String role,
                           Model model) {

        // 验证用户名是否为空
        if (username == null || username.trim().isEmpty()) {
            model.addAttribute("errorMsg", "用户名不能为空");
            model.addAttribute("username", username);
            return "register";
        }

        // 验证密码
        if (password == null || password.length() < 4) {
            model.addAttribute("errorMsg", "密码长度至少4位");
            model.addAttribute("username", username);
            return "register";
        }

        // 验证两次密码一致
        if (!password.equals(confirmPassword)) {
            model.addAttribute("errorMsg", "两次密码输入不一致");
            model.addAttribute("username", username);
            return "register";
        }

        // 验证用户名是否已存在
        if (userService.findByUsername(username.trim()) != null) {
            model.addAttribute("errorMsg", "用户名已存在");
            model.addAttribute("username", username);
            return "register";
        }

        // 创建用户
        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(password);
        try {
            user.setRole(Role.valueOf(role));
        } catch (IllegalArgumentException e) {
            user.setRole(Role.STUDENT);
        }

        userService.register(user);
        
        // 【关键】注册学生账号时，同时创建 student 记录（否则签到会提示"学生不存在"）
        if (user.getRole() == Role.STUDENT) {
            Student student = new Student();
            student.setStudentNo(username.trim());    // 用用户名（学号）作为 studentNo
            student.setName(username.trim());          // 默认为用户名，可在学生管理中修改
            student.setStatus(1);                      // 默认启用
            // 关联 User 账号
            User savedUser = userService.findByUsername(username.trim());
            if (savedUser != null) {
                student.setUserId(savedUser.getId());
            }
            studentService.save(student);
        }
        
        model.addAttribute("successMsg", "注册成功，请登录");
        return "login";
    }
}