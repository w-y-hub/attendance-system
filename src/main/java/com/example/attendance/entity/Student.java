package com.example.attendance.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "student")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 学号
    @NotBlank(message = "学号不能为空")
    @Column(name = "student_no", nullable = false, unique = true, length = 50)
    private String studentNo;

    // 姓名
    @NotBlank(message = "姓名不能为空")
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    // 班级
    @NotBlank(message = "班级不能为空")
    @Column(name = "class_name", nullable = false, length = 50)
    private String className;

    // 性别
    @NotBlank(message = "性别不能为空")
    @Column(name = "gender", length = 10)
    private String gender;

    // 出生日期
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "birth_date")
    private LocalDate birthDate;

    // 手机号
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    @Column(name = "phone", length = 20)
    private String phone;

    // 邮箱
    @Email(message = "邮箱格式不正确")
    @Column(name = "email", length = 100)
    private String email;

    // 状态：1正常，0禁用等
    @Column(name = "status")
    private Integer status;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    public Student() {
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createTime = now;
        this.updateTime = now;
        if (this.status == null) {
            this.status = 1;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

}