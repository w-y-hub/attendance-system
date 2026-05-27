package com.example.attendance.entity;

/**
 * 学生实体类 —— 对应数据库中的 student 表
 *
 * 【关于 @Entity 注解】
 * 加上 @Entity 后，Spring Boot 启动时会自动在数据库中创建对应的表（因为 application.properties
 * 设置了 spring.jpa.hibernate.ddl-auto=update）。JPA（Java Persistence API）会把 Java 对象
 * 自动映射到关系型数据库的表中。
 *
 * 【命名规范】
 * 属性名采用小驼峰命名（如 studentNo），数据库列名用下划线分隔（student_no），
 * 通过 @Column(name = "student_no") 来指定映射关系。
 */

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity                     // ← 告诉 Spring 这是一个 JPA 实体类
@Table(name = "student")    // ← 指定对应的数据库表名
public class Student {

    // ===================== 核心字段 =====================

    @Id     // ← 主键
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ← 自增主键（数据库自动生成）
    private Long id;

    /**
     * 学号 —— 每个学生唯一，不能为空。
     * unique = true 会在数据库层面加唯一约束。
     */
    @NotBlank(message = "学号不能为空")
    @Column(name = "student_no", nullable = false, unique = true, length = 50)
    private String studentNo;

    /** 姓名 */
    @NotBlank(message = "姓名不能为空")
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    /** 性别 */
    @NotBlank(message = "性别不能为空")
    @Column(name = "gender", length = 10)
    private String gender;

    // ===================== 新增字段（学院、年级、专业） =====================

    /**
     * 学院 —— 如：计算机与人工智能学院
     * 新增字段，对应 Excel 导入的第4列。
     */
    @Column(name = "college", length = 100)
    private String college;

    /**
     * 年级 —— 如：2022
     * 注意：虽然叫"年级"，但实际存储的是入学年份的字符串表示。
     */
    @Column(name = "grade", length = 20)
    private String grade;

    /**
     * 专业 —— 如：计算机科学与技术
     * 这是学生所属的具体专业方向。
     */
    @Column(name = "major", length = 100)
    private String major;

    // ===================== 原有可选字段 =====================

    /** 班级（如：2022级计算机科学与技术） */
    @NotBlank(message = "班级不能为空")
    @Column(name = "class_name", nullable = false, length = 50)
    private String className;

    /** 状态：1=正常，0=禁用（默认启用） */
    @Column(name = "status")
    private Integer status;

    // ===================== 时间戳 =====================

    /** 创建时间 —— @PrePersist 会在插入前自动设置 */
    @Column(name = "create_time")
    private LocalDateTime createTime;

    /** 更新时间 —— @PreUpdate 会在更新前自动设置 */
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    // ===================== 构造方法 =====================

    /** JPA 要求必须有一个无参构造方法 */
    public Student() {
    }

    // ===================== 生命周期回调 =====================

    /**
     * @PrePersist：在第一次保存（INSERT）之前自动执行。
     * 这里设置了创建时间和更新时间，并确保 status 默认是 1（正常）。
     * 【为什么用 @PrePersist 而不是在代码里手动 set？】
     * 因为这段逻辑和实体绑定在一起，不容易遗漏，也避免在多个 Service 里重复写。
     */
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createTime = now;
        this.updateTime = now;
        if (this.status == null) {
            this.status = 1;   // 默认启用
        }
    }

    /** @PreUpdate：每次更新（UPDATE）之前自动更新时间戳 */
    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }

    // ===================== Getter / Setter =====================

    /**
     * 【关于 Getter/Setter】
     * Spring Boot 和 Thymeleaf 通过 getXxx() 来读取属性（如 student.name），
     * 通过 setXxx() 来设置属性。这就是"JavaBean 规范"。
     * 如果你用了 Lombok（@Data），这些就不用手写了，但本项目要求不依赖 Lombok。
     */

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = college; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
