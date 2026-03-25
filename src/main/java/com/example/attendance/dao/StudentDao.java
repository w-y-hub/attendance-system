package com.example.attendance.dao;

import com.example.attendance.entity.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class StudentDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    public void Insert(Student student){
        String sql = "INSERT INTO student(studentID,studentName,studentClassName,age) VALUES(?,?,?,?)";
        jdbcTemplate.update(sql,student.getStudentId(),student.getStudentName(),student.getClassName(),student.getAge());
    }
}
