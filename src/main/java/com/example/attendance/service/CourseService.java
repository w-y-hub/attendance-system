package com.example.attendance.service;

import com.example.attendance.entity.Course;
import com.example.attendance.repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 课程业务层 —— 课程增删改查
 *
 * 【@Service 注解】
 * 告诉 Spring 这是一个业务服务类，Spring 会自动管理它的实例。
 * 通过构造方法注入 CourseRepository。
 */
@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    /** 查询所有课程 */
    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    /** 根据 ID 查询单门课程 */
    public Course findById(Long id) {
        return courseRepository.findById(id).orElse(null);
    }

    /** 根据课程名称查找（用于注册时自动分配班级） */
    public List<Course> findByCourseName(String courseName) {
        return courseRepository.findByCourseName(courseName);
    }

    /** 根据教师ID查询该教师的课程 */
    public List<Course> findByTeacherId(Long teacherId) {
        return courseRepository.findByTeacherId(teacherId);
    }

    /** 根据班级名称查询课程（用于统计期望出勤） */
    public List<Course> findByClassName(String className) {
        return courseRepository.findByClassName(className);
    }

    /** 保存课程（新增或更新） */
    public Course save(Course course) {
        return courseRepository.save(course);
    }

    /** 删除课程 */
    public void deleteById(Long id) {
        courseRepository.deleteById(id);
    }
}