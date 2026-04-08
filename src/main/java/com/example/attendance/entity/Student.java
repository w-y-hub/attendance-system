package com.example.attendance.entity;

public class Student {
    private String studentId;
    private String studentName;
    private String className;
    private Integer age;

    public Student() {
    }

    public Student(String studentId, String studentName, String className, Integer age) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.className = className;
        this.age = age;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
