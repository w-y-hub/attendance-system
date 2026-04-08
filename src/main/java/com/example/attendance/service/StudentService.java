package com.example.attendance.service;
import com.example.attendance.entity.Student;

import java.util.List;


public interface StudentService{
    String addStudent(Student student);
    Student getById(Long id);
    List<Student> findAll();
    void update(Student student);
    void deleteByStudentId(String studentId);
}


