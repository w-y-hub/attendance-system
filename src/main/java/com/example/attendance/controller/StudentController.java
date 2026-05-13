package com.example.attendance.controller;

import com.example.attendance.entity.Student;
import com.example.attendance.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private StudentService studentService;

    // 学生列表
    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("students", studentService.findAll());
        return "student-list";
    }

    // 新增页面
    @GetMapping("/add")
    public String addPage(Model model) {
        model.addAttribute("student", new Student());
        model.addAttribute("title", "新增学生");
        return "student-form";
    }

    // 保存新增学生
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("student") Student student,
                       BindingResult bindingResult,
                       Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("title", "新增学生");
            return "student-form";
        }

        if (studentService.existsByStudentNo(student.getStudentNo())) {
            model.addAttribute("title", "新增学生");
            model.addAttribute("studentNoError", "学号已存在");
            return "student-form";
        }

        studentService.save(student);
        return "redirect:/student/list";
    }

    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable Long id, Model model) {
        Student student = studentService.getById(id);
        if (student == null) {
            return "redirect:/student/list";
        }
        model.addAttribute("student", student);
        model.addAttribute("title", "编辑学生");
        return "student-form";
    }
    @PostMapping("/update")
    public String update(@Valid @ModelAttribute("student") Student student,
                         BindingResult bindingResult,
                         Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("title", "编辑学生");
            return "student-form";
        }

        Student oldStudent = studentService.getByStudentNo(student.getStudentNo());
        if (oldStudent != null && !oldStudent.getId().equals(student.getId())) {
            model.addAttribute("title", "编辑学生");
            model.addAttribute("studentNoError", "学号已存在");
            return "student-form";
        }

        studentService.update(student);
        return "redirect:/student/list";
    }
}