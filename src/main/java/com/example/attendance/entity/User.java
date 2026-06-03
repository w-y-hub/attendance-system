package com.example.attendance.entity;

import jakarta.persistence.*;

/**
 * 用户实体 —— 对应数据库 users 表
 *
 * 这个实体存的是"登录账号"信息，不是学生信息。
 * 学生信息在 Student 实体中，两者是分开的。
 *
 * 【为什么 User 和 Student 分开？】
 * User   → 登录用（username + password + role）
 * Student → 管理用（学号、姓名、班级、学院等）
 * 一个 Student 对应一个 User（用学号关联），但职责不同。
 *
 * 【@Enumerated(EnumType.STRING)】
 * 指定枚举存为字符串（如 "ADMIN"）而不是数字序号（0,1,2），
 * 数据库里可读性更好。
 */

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    /** 用户真实姓名（ADMIN 用管理员真名，TEACHER 用教师名，STUDENT 用学生名） */
    @Column(name = "name", length = 50)
    private String name;

    @Column(nullable = false)
    private Boolean enabled = true;

    public User() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}