package com.example.attendance.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/login")
    public String loginPage(Model model) {
        return "login"; // 对应 templates/login.html
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register"; // 对应 templates/register.html
    }

    @GetMapping("/")
    public String indexPage() {
        return "index"; // 对应 templates/index.html
    }
}