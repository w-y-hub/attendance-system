package com.example.attendance.dao;

import com.example.attendance.entity.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StudentDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void Insert(Student student){
        String sql = "INSERT INTO student(student_id,student_name,class_name,age) VALUES(?,?,?,?)";
        jdbcTemplate.update(sql,student.getStudentId(),student.getStudentName(),student.getClassName(),student.getAge());
    }

    public Student findById(Long id) {
        String sql = "select * from student where id = ?";
        return jdbcTemplate.queryForObject(
                sql,
                new BeanPropertyRowMapper<>(Student.class),
                id
        );
    }

    public List<Student> findAll() {
        String sql = "select student_id, student_name, class_name, age from student";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Student.class));
    }

    public void update(Student student) {
        String sql = "update student set student_name = ?, class_name = ?, age = ? where student_id = ?";
        jdbcTemplate.update(sql,
                student.getStudentName(),
                student.getClassName(),
                student.getAge(),
                student.getStudentId());
    }

    public void deleteByStudentId(String studentId) {
        String sql = "delete from student where student_id = ?";
        jdbcTemplate.update(sql, studentId);
    }
}
