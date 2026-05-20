package com.example.attendance.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_no", length = 50, nullable = false)
    private String studentNo;

    @Column(name = "student_name", length = 50)
    private String studentName;

    @Column(name = "class_name", length = 50)
    private String className;

    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    // 签到状态：NORMAL / LATE / ABSENT
    @Column(name = "status", length = 20, nullable = false)
    private String status;

    // 签到时间
    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    // 签退时间
    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    // 是否早退
    @Column(name = "early_leave")
    private Boolean earlyLeave = false;

    @Column(name = "remark", length = 255)
    private String remark;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createTime == null) {
            createTime = now;
        }
        if (updateTime == null) {
            updateTime = now;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updateTime = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getStudentNo() { return studentNo; }
    public String getStudentName() { return studentName; }
    public String getClassName() { return className; }
    public Long getCourseId() { return courseId; }
    public LocalDate getAttendanceDate() { return attendanceDate; }
    public String getStatus() { return status; }
    public LocalDateTime getCheckInTime() { return checkInTime; }
    public LocalDateTime getCheckOutTime() { return checkOutTime; }
    public Boolean getEarlyLeave() { return earlyLeave; }
    public String getRemark() { return remark; }
    public LocalDateTime getCreateTime() { return createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }

    public void setId(Long id) { this.id = id; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public void setClassName(String className) { this.className = className; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }
    public void setStatus(String status) { this.status = status; }
    public void setCheckInTime(LocalDateTime checkInTime) { this.checkInTime = checkInTime; }
    public void setCheckOutTime(LocalDateTime checkOutTime) { this.checkOutTime = checkOutTime; }
    public void setEarlyLeave(Boolean earlyLeave) { this.earlyLeave = earlyLeave; }
    public void setRemark(String remark) { this.remark = remark; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}