package com.example.attendance.controller;

import com.example.attendance.entity.User;
import com.example.attendance.service.UserService;
import com.example.attendance.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller // 注意：保持为 @Controller
public class AuthController {

    @Autowired
    private UserService userService;

    // 删除了 @GetMapping("/login") 和 @GetMapping("/register") 页面路由

    @PostMapping("/register")
    @ResponseBody // 关键：这一行保证返回的是 JSON 而不是视图
    public Result register(@RequestBody User user) {
        userService.register(user);
        return Result.success("注册成功");
    }
}