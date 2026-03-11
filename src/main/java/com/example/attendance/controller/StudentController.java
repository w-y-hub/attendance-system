package com.example.attendance.controller;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class StudentController {
    String student_name = "WY";
    int student_ID = 42411070;
    String student_clas = "i104";
    List<String>  courses = new ArrayList<>();

    @GetMapping("/student/info")
    public String studentInfo(){
        return student_name+" "+student_ID+" "+ student_clas;
    }
    @PostMapping("/student/attendance")
    public String studentAttendance(@RequestBody String s){
        return "学号为："+s+"的学生已经打卡成功";
    }
    @GetMapping("/student/courses")
    public List studentCourses(){
        courses.add("JAVAEE");
        return courses;
    }
}
