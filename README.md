# 班级考勤管理系统

## 项目简介
班级考勤管理系统是一个基于SpringBoot开发的Web应用，用于管理学生考勤记录。系统支持**三种角色**：管理员、教师、学生，各自拥有不同的权限和功能界面。本项目面向Spring Boot初学者，代码包含详细的注释说明。

---

## 技术栈

- **后端**：Spring Boot 3.3, Spring Security 6, Spring Data JPA
- **前端**：Thymeleaf, Bootstrap 5 (WebJars), Chart.js
- **数据库**：PostgreSQL
- **构建工具**：Maven
- **其他**：Apache POI (Excel解析), BCrypt (密码加密)

---

## 功能特性

### 按角色划分

| 功能 | 管理员 | 教师 | 学生 |
|------|--------|------|------|
| 教师账号管理 | ✅ | — | — |
| 课程管理（创建/分配教师） | ✅ | — | — |
| 学生信息管理 | ✅ | ✅（本班） | — |
| Excel批量导入学生 | ✅ | — | — |
| 考勤打卡/签退 | — | — | ✅ |
| 个人考勤记录查看 | ✅（全部） | — | ✅（自己） |
| 考勤统计（按周/月） | ✅ | ✅ | ✅ |
| 班级考勤统计 | ✅ | ✅ | — |
| 查看授课班级 | — | ✅ | — |
| 班级学生管理 | — | ✅ | — |
| 考勤数据导入 | ✅ | ✅ | — |
| CSV导出 | ✅ | ✅ | ✅ |

---

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+（或使用项目自带的 `mvnw`）
- PostgreSQL（建议 14+）

### 安装步骤

1. **克隆或下载项目**

2. **创建数据库**

```bash
createdb -U postgres springboot_course
```
或者在 pgAdmin 中新建数据库 `springboot_course`。

3. **修改配置文件**

打开 `src/main/resources/application.properties`，将数据库密码改为你自己的：

```properties
spring.datasource.password=你的密码
```

如果 PostgreSQL 端口不是 5433，也需要修改：

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/springboot_course
```

4. **运行项目**

```bash
./mvnw spring-boot:run
```

5. **访问系统**

浏览器打开 http://localhost:8080

### 测试账号

所有账号密码统一为 `123456`。

| 角色 | 用户名 | 姓名 |
|------|--------|------|
| 管理员 | `admin` | 系统管理员 |
| 教师 | `teacher1` | 张教授 |
| 学生 | `42211077` | 孙涛涛 |

---

## 数据库结构

系统包含 **5 张数据表**，由 JPA 自动创建（`ddl-auto=update`）：

| 表名 | 说明 | 主要字段 |
|------|------|---------|
| `users` | 登录账号 | username, password(BCrypt), role, name |
| `student` | 学生信息 | student_no(学号), name, gender, class_name, college, grade, major |
| `teacher` | 教师信息 | name, title(职称) |
| `course` | 课程信息 | course_name, class_name, teacher_id, start_time, end_time |
| `attendance` | 考勤记录 | student_no, course_id, attendance_date, status, check_in/out_time |

### 表关系图

```
users（登录账号）
  ├── role=STUDENT → student（学生信息）→ attendance（考勤记录）
  ├── role=TEACHER → teacher（教师信息）→ course（课程）
  └── role=ADMIN  → 无扩展表
```

---

## 项目结构

```
com.example.attendance/
├── AttendanceSystemApplication.java   ← 启动入口
├── config/                            ← 配置层
│   └── SecurityConfig.java            ← Spring Security（角色权限+CSRF）
├── controller/                        ← 控制层
│   ├── AuthController.java            ← 注册
│   ├── HomeController.java            ← 首页（角色感知仪表盘）
│   ├── PageController.java            ← 登录/注册页面路由
│   ├── StudentController.java         ← 学生管理（仅ADMIN）
│   ├── AttendanceController.java      ← 考勤打卡/列表/统计/导入导出
│   ├── AdminController.java           ← 教师管理、课程管理（仅ADMIN）
│   └── TeacherController.java         ← 教师课程/班级考勤/学生管理
├── service/                           ← 业务层
│   ├── UserService.java               ← 用户注册（BCrypt加密）
│   ├── StudentService.java            ← 学生CRUD + Excel导入
│   ├── AttendanceService.java         ← 签到/签退/统计/导入
│   ├── CourseService.java             ← 课程管理
│   ├── TeacherService.java            ← 教师管理
│   └── security/
│       └── CustomUserDetailsService.java  ← Spring Security认证
├── repository/                        ← 数据访问层
│   ├── UserRepository.java
│   ├── StudentRepository.java
│   ├── AttendanceRepository.java
│   ├── CourseRepository.java
│   └── TeacherRepository.java
├── entity/                            ← 实体类
│   ├── User.java / Student.java / Teacher.java
│   ├── Course.java / Attendance.java
│   └── Role.java                      ← 角色枚举
├── dto/                               ← 数据传输对象
├── exception/
│   └── GlobalExceptionHandler.java    ← 全局异常处理
└── util/
    ├── ExcelUtils.java                ← Excel解析工具
    ├── PasswordEncoderUtil.java       ← 密码加密工具
    └── Result.java                    ← JSON统一响应
```

---

## 核心页面路由

| 路由 | 功能 | 访问角色 |
|------|------|---------|
| `/login` | 登录页 | 公开 |
| `/register` | 注册页 | 公开 |
| `/home` | 首页仪表盘 | 登录用户 |
| `/attendance/checkIn` | 签到/签退 | STUDENT |
| `/attendance/list` | 考勤记录列表 | 登录用户 |
| `/attendance/statistics` | 个人统计+搜索 | 登录用户 |
| `/attendance/import` | 考勤导入 | ADMIN/TEACHER |
| `/attendance/export` | 导出CSV | 登录用户 |
| `/admin/teachers` | 教师管理 | ADMIN |
| `/admin/courses` | 课程管理 | ADMIN |
| `/teacher/attendance` | 班级考勤统计 | TEACHER |
| `/teacher/students` | 班级学生管理 | TEACHER |
| `/student/list` | 学生管理 | ADMIN |

---

## 分层架构说明

```
Controller（控制层）—— 接收请求参数，调用Service，返回视图
      ↓
Service（业务层）   —— 封装业务逻辑，调用Repository
      ↓
Repository（数据层）—— 操作数据库（JPA自动实现）
```

**规范：**
- Controller 不能直接调用 Repository
- Controller 不能写业务逻辑（如时间校验、状态判定）
- Service 方法返回 String 表示错误信息，返回 null 表示成功

---

## 部署说明

### 方式一：直接运行

```bash
./mvnw spring-boot:run
```

### 方式二：打包后运行

```bash
./mvnw package -DskipTests
java -jar target/attendance-system-0.0.1-SNAPSHOT.jar
```

### 方式三：IDE运行

在 IntelliJ IDEA 或 Eclipse 中导入 Maven 项目，运行 `AttendanceSystemApplication.java` 的 `main` 方法。

---

## 联系方式

- 作者：汪洋
- 邮箱：wy.wang_@outlook.com
