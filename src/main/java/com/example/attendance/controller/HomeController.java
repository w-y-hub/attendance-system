package com.example.attendance.controller;

import com.example.attendance.entity.Course;
import com.example.attendance.entity.Role;
import com.example.attendance.entity.User;
import com.example.attendance.service.CourseService;
import com.example.attendance.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;

/**
 * 首页控制器 —— 登录后的仪表盘页面
 *
 * 【角色感知】
 * 这个控制器会读取当前登录用户的角色（ADMIN / TEACHER / STUDENT），
 * 并将角色信息传给模板，让模板根据角色显示不同的内容。
 *
 * 在 Thymeleaf 中通过 ${userRole} 判断：
 *   <div th:if="${userRole == 'ADMIN'}">管理员菜单</div>
 *   <div th:if="${userRole == 'TEACHER'}">教师菜单</div>
 *   <div th:if="${userRole == 'STUDENT'}">学生菜单</div>
 */

@Controller
public class HomeController {

    private final CourseService courseService;
    private final UserService userService;

    public HomeController(CourseService courseService, UserService userService) {
        this.courseService = courseService;
        this.userService = userService;
    }

    /**
     * 首页仪表盘
     * 根据当前用户角色展示不同的仪表盘内容。
     *
     * @param model     向模板传数据的容器
     * @param principal Spring Security 自动注入的当前登录用户
     */
    @GetMapping("/home")
    public String home(Model model, Principal principal) {
        // 获取当前登录用户名
        String username = principal != null ? principal.getName() : "用户";

        // 从数据库查出完整的用户信息（包括角色、真实姓名）
        String userRole = "STUDENT";     // 默认角色
        String realName = username;       // 默认显示用户名
        if (principal != null) {
            User user = userService.findByUsername(username);
            if (user != null) {
                userRole = user.getRole().name();
                realName = user.getName() != null ? user.getName() : username;
            }
        }

        // 获取课程总数
        List<Course> courses = courseService.findAll();

        // 把数据传到模板
        model.addAttribute("username", username);
        model.addAttribute("realName", realName);
        model.addAttribute("userRole", userRole);
        model.addAttribute("courseCount", courses.size());

        return "home";
    }
}