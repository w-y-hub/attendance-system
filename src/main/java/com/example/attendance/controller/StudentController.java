package com.example.attendance.controller;

import com.example.attendance.dto.StudentImportResult;
import com.example.attendance.entity.Student;
import com.example.attendance.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
        Student student = studentService.findById(id);
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

        Student oldStudent = studentService.findByStudentNo(student.getStudentNo());
        if (oldStudent != null && !oldStudent.getId().equals(student.getId())) {
            model.addAttribute("title", "编辑学生");
            model.addAttribute("studentNoError", "学号已存在");
            return "student-form";
        }

        studentService.update(student);
        return "redirect:/student/list";
    }

    /**
     * 这是在干什么：
     * 打开学生 Excel 导入页面。
     *
     * 如何实现：
     * 返回一个 Thymeleaf 页面，让用户选择并上传 Excel 文件。
     */
    @GetMapping("/import")
    public String importPage() {
        return "student-import";
    }

    /**
     * 这是在干什么：
     * 接收用户上传的 Excel 文件，并调用业务层完成导入。
     *
     * 如何实现：
     * 1. 判断文件是否为空
     * 2. 判断文件扩展名是否为 .xlsx
     * 3. 调用 studentService.importStudents(file)
     * 4. 把导入结果返回页面显示
     */
    @PostMapping("/import")
    public String importStudents(@RequestParam("file") MultipartFile file, Model model) {
        if (file == null || file.isEmpty()) {
            model.addAttribute("error", "请选择要上传的 Excel 文件");
            return "student-import";
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".xlsx")) {
            model.addAttribute("error", "目前只支持 .xlsx 格式的 Excel 文件");
            return "student-import";
        }


        //  判断 contentType（辅助校验，不绝对依赖）
        String contentType = file.getContentType();
        if (contentType != null &&
                !contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
            // 这里只做辅助，不强制 return，避免某些浏览器传的 MIME 不标准
        }

        StudentImportResult result = studentService.importStudents(file);
        model.addAttribute("result", result);

        return "student-import";
    }
}