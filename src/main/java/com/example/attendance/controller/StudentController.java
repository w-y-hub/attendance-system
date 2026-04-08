package com.example.attendance.controller;
import com.example.attendance.entity.*;
import com.example.attendance.service.StudentService;
import com.example.attendance.util.Result;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/student")
public class StudentController {
    @Autowired
    private StudentService studentService;

    @PostMapping
    public Result<String> addStudent(@RequestBody Student student){
        return Result.success(studentService.addStudent(student));
    }

    @GetMapping("/{id}")
    public Result<Student> getById(@PathVariable Long id) {
        Student student = studentService.getById(id);
        if (student == null) {
            return Result.error("学生不存在");
        }
        return Result.success(student);
    }

    @GetMapping
    public List<Student> findAll() {
        return studentService.findAll();
    }

    @PutMapping
    public String update(@RequestBody Student student) {
        studentService.update(student);
        return "更新成功";
    }

    @DeleteMapping("/{studentId}")
    public String deleteByStudentId(@PathVariable String studentId) {
        studentService.deleteByStudentId(studentId);
        return "删除成功";
    }
}