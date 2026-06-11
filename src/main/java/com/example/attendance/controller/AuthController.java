package com.example.attendance.controller;

import com.example.attendance.entity.*;
import com.example.attendance.service.*;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * 用户认证控制器 —— 处理注册请求
 *
 * 【注册流程】
 * 1. register.html 表单提交 → POST /register
 * 2. 校验用户名、密码、确认密码
 * 3. 创建 User 记录（写入 users 表）
 * 4. 同步创建 Student 记录（写入 student 表，防止签到查不到学生）
 * 5. 重定向到登录页，显示"注册成功"
 */
@Controller
public class AuthController {

    @Autowired private UserService userService;
    @Autowired private StudentService studentService;
    @Autowired private CourseService courseService;

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           Model model) {

        // ===== 参数校验 =====
        if (username == null || username.trim().isEmpty()) {
            model.addAttribute("errorMsg", "用户名不能为空");
            return "register";
        }
        if (password == null || password.length() < 4) {
            model.addAttribute("errorMsg", "密码长度至少4位");
            return "register";
        }
        if (!password.equals(confirmPassword)) {
            model.addAttribute("errorMsg", "两次密码输入不一致");
            return "register";
        }
        if (userService.findByUsername(username.trim()) != null) {
            model.addAttribute("errorMsg", "用户名已存在");
            return "register";
        }

        // ===== 创建 User 账号 =====
        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(password);
        user.setRole(Role.STUDENT);
        user.setName(username.trim());
        User savedUser = userService.register(user);   // 用返回值，保证拿到自增ID

        // ===== 同步创建 Student 记录 =====
        Student student = new Student();
        student.setStudentNo(username.trim());
        student.setName(username.trim());
        student.setGender("");
        student.setStatus(1);
        student.setUserId(savedUser.getId());          // 用保存后返回对象的 ID

        // 查找"定向越野"课程，获取班级名
        List<Course> courses = courseService.findByCourseName("定向越野");
        student.setClassName(courses.isEmpty() ? "定向越野班" : courses.get(0).getClassName());

        studentService.save(student);

        // ===== 重定向到登录页 =====
        model.addAttribute("successMsg", "注册成功，请登录");
        return "login";
    }
}