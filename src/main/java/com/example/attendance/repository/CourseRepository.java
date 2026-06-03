package com.example.attendance.repository;

import com.example.attendance.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 课程数据访问层
 *
 * 课程数据由管理员维护，教师可以查看自己的课程。
 */
public interface CourseRepository extends JpaRepository<Course, Long> {

    /** 根据课程名称查找（可能有多条同名记录） */
    List<Course> findByCourseName(String courseName);

    /** 根据授课教师ID查找课程 */
    List<Course> findByTeacherId(Long teacherId);

    /** 根据上课班级查找课程（用于统计期望出勤） */
    List<Course> findByClassName(String className);
}