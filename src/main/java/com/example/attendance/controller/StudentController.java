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


}