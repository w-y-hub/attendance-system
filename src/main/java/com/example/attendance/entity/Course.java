package com.example.attendance.entity;

/**
 * 课程实体 —— 对应数据库 course 表
 *
 * 一门课程包含课程名称、上课班级、上课时间段。
 * startTime / endTime 是 LocalTime 类型（只存时分秒，不存日期），
 * 因为课程时间是每天固定的（如 08:30-10:00），不随日期变化。
 *
 * 【思考：为什么用 LocalTime 而不是 LocalDateTime？】
 * 课程时间是周期性重复的（每周几几点到几点），
 * 如果存 LocalDateTime 会绑定到具体某一天，不方便。
 */

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "course")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_name", length = 100, nullable = false)
    private String courseName;

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "class_name", length = 50)
    private String className;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    /** 授课教师ID（关联 Teacher 表） */
    @Column(name = "teacher_id")
    private Long teacherId;

    /** 教师姓名（冗余字段，方便列表显示，不用每次联表查询） */
    @Column(name = "teacher_name", length = 50)
    private String teacherName;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    public void prePersist() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getClassName() {
        return className;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}