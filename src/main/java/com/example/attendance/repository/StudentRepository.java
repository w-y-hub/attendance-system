package com.example.attendance.repository;

import com.example.attendance.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long>,
        JpaSpecificationExecutor<Student>,
        QueryByExampleExecutor<Student> {

    // 根据学号查询
    Student findByStudentNo(String studentNo);

    // 根据班级查询
    List<Student> findByClassName(String className);

    // 判断学号是否存在
    boolean existsByStudentNo(String studentNo);
}