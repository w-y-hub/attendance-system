package com.example.attendance.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 页面路由控制器 —— 负责简单的页面跳转
 *
 * 【@Controller 注解】
 * 告诉 Spring 这是一个"控制器"，Spring 会扫描到它，
 * 并将 @GetMapping 等注解配置到 Tomcat 的路由表中。
 *
 * 【@Controller vs @RestController】
 * @Controller：方法返回"视图名称"（HTML 页面的文件名），Thymeleaf 会渲染成 HTML
 * @RestController：方法直接返回"数据"（JSON/XML），不经过视图解析器
 *
 * @Controller 的方法如果加了 @ResponseBody，也可以返回 JSON，
 * AuthController 中的 register 方法就用了这个技巧。
 */

@Controller
public class PageController {

    /**
     * 登录页面
     * 访问 /login 时返回 login.html
     * 注意：这里只负责"展示页面"，实际的登录验证由 Spring Security 处理
     */
    @GetMapping("/login")
    public String loginPage(Model model) {
        return "login";
    }

    /**
     * 注册页面
     * 访问 /register 时返回 register.html
     * 表单提交到 POST /register，由 AuthController 处理
     */
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    /**
     * 系统首页（未登录时看到）
     * 访问 / 时返回 index.html——一个带登录/注册按钮的欢迎页
     */
    @GetMapping("/")
    public String indexPage() {
        return "index";
    }
}