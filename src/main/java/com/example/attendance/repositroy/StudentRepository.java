package com.example.attendance.repositroy;

import com.example.attendance.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    // 根据学号查询
    Student findByStudentNo(String studentNo);

    // 根据班级查询
    List<Student> findByClassName(String className);
}