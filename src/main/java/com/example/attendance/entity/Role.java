package com.example.attendance.entity;

/**
 * 用户角色枚举 —— 用户身份类型
 *
 * 【什么是枚举（enum）？】
 * 枚举是一种特殊的类，它的取值是固定的几个常量。
 * 比用 String 更安全，因为不会拼错（比如不会出现 "Admin" 和 "admin" 不一致）。
 *
 * 【@Enumerated(EnumType.STRING)】
 * 在 User.java 中，role 字段用 @Enumerated(EnumType.STRING)
 * 指定存到数据库的是枚举的名称字符串（如 "ADMIN"），而不是数字序号。
 * 好处：数据库里直接看到 ADMIN / TEACHER / STUDENT，可读性强。
 *
 * 当前定义的角色：
 *   ADMIN   — 管理员，拥有所有权限
 *   TEACHER — 教师，可以查看所教班级的考勤
 *   STUDENT — 学生，可以进行签到/签退/查看自己的考勤记录
 */
public enum Role {
    ADMIN,
    TEACHER,
    STUDENT
}