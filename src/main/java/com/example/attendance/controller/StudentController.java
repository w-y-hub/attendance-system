package com.example.attendance.controller;

import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.Student;
import com.example.attendance.service.StudentService;
import com.example.attendance.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private StudentService studentService;

    // 新增学生
    @PostMapping
    public Result<String> addStudent(@RequestBody Student student) {
        return Result.success(studentService.addStudent(student));
    }

    // 根据 id 查询
    @GetMapping("/{id}")
    public Result<Student> getById(@PathVariable Long id) {
        Student student = studentService.getById(id);
        if (student == null) {
            return Result.error("学生不存在");
        }
        return Result.success(student);
    }

    // 查询所有
    @GetMapping
    public Result<List<Student>> findAll() {
        return Result.success(studentService.findAll());
    }

    // 更新
    @PutMapping
    public Result<String> update(@RequestBody Student student) {
        studentService.update(student);
        return Result.success("更新成功");
    }

    // 根据 id 删除
    @DeleteMapping("/{id}")
    public Result<String> deleteById(@PathVariable Long id) {
        studentService.deleteById(id);
        return Result.success("删除成功");
    }

    // 根据学号查询
    @GetMapping("/studentNo/{studentNo}")
    public Result<Student> getByStudentNo(@PathVariable String studentNo) {
        Student student = studentService.getByStudentNo(studentNo);
        if (student == null) {
            return Result.error("学生不存在");
        }
        return Result.success(student);
    }

    // 根据班级查询
    @GetMapping("/class")
    public Result<List<Student>> getByClassName(@RequestParam String className) {
        return Result.success(studentService.getByClassName(className));
    }
}