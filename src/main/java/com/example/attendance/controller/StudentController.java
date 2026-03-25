package com.example.attendance.controller;
import com.example.attendance.entity.*;
import com.example.attendance.util.Result;
import jakarta.annotation.PostConstruct;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class StudentController {

    private List<Student> studentList = new ArrayList<>();
    private List<Attendance> attendanceList = new ArrayList<>();

    @PostConstruct
    public void initData() {
        studentList.add(new Student("2023001", "zhangsan", "i104",18));
        studentList.add(new Student("2023002", "lisi", "i104",18));
        studentList.add(new Student("2023003", "wangwu", "i105",18));
    }

    @GetMapping("/student/info/{id}")
    public Result<Student> getStudentById(@PathVariable String id){
        for(Student s : studentList){
            if(s.getStudentId().equals(id)){
                return Result.success(s);
            }
        }
        return Result.success(null);
    }

    @GetMapping("/student/list")
    public Result<List<Student>> searchStudent(
            @RequestParam String className,
            @RequestParam(defaultValue = "1") int page ){
        List<Student> resultList = studentList.stream()
                .filter(s -> s.getClassName().equals(className))
                .collect(Collectors.toList());

        return Result.success(resultList);//此处返回了列表中所有学生

    }

    @PostMapping("attendance/update")
    public Result<String> crete(@RequestBody Attendance attendance){
        System.out.println("当前记录总数：" + attendanceList.size());

        return Result.success("学号：" + attendance.getStudentId() + " 考勤记录已更新");
    }


}