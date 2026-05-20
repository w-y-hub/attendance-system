package com.example.attendance.controller;

import com.example.attendance.entity.Course;
import com.example.attendance.service.CourseService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;

@Controller
public class HomeController {

    private final CourseService courseService;

    public HomeController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping("/home")
    public String home(Model model, Principal principal) {
        String username = principal != null ? principal.getName() : "用户";
        List<Course> courses = courseService.findAll();

        model.addAttribute("username", username);
        model.addAttribute("courseCount", courses.size());

        return "home";
    }
}