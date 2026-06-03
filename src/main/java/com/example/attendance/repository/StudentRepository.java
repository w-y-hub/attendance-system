package com.example.attendance.repository;

/**
 * 学生数据访问层
 *
 * 继承 JpaRepository + JpaSpecificationExecutor：
 *   JpaRepository<Student, Long>       → 基础 CRUD（增删改查）
 *   JpaSpecificationExecutor<Student>  → 支持动态条件查询（Specification）
 *
 * 【什么时候用 JpaSpecificationExecutor？】
 * 当查询条件不固定（用户可能按姓名查、可能按班级查、可能按日期范围查），
 * 用 Specification 可以在代码里动态拼接 WHERE 子句。
 * 本项目中考勤记录列表的筛选就用到了这个特性。
 */

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

    // 按姓名模糊查询（用于统计页搜索）
    List<Student> findByNameContaining(String name);


}