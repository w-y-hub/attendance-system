# 学生考勤管理系统 —— Spring Boot 初学者项目

一个基于 **Spring Boot 3 + Spring Security + JPA + Thymeleaf + PostgreSQL** 的班级考勤管理系统。

---

## 🚀 快速启动

### 环境要求
- JDK 17+
- PostgreSQL（默认端口 **5433**，数据库名 **springboot_course**）
- Maven（或使用项目自带的 `mvnw`）

### 第一步：创建数据库

```sql
-- 方式一：命令行
createdb -U postgres springboot_course

-- 方式二：用 pgAdmin 图形界面创建数据库 springboot_course
```

### 第二步：修改数据库密码

打开 `src/main/resources/application.properties`，把密码改成你自己的：

```properties
spring.datasource.username=postgres
spring.datasource.password=你的数据库密码
```

### 第三步：运行

```bash
./mvnw clean compile      # 编译
./mvnw spring-boot:run    # 启动
```

浏览器访问：**http://localhost:8080**

---

## 📁 项目结构（包分层架构）

Spring Boot 推荐"按层分包"（也叫"分层架构"），每一层各司其职：

```
com.example.attendance/
├── AttendanceSystemApplication.java   ← 入口类
├── config/                            ← 配置层
│   └── SecurityConfig.java            ← Spring Security 安全配置
├── controller/                        ← 控制层（接收请求、返回页面）
│   ├── AuthController.java            ← 登录/注册
│   ├── HomeController.java            ← 首页
│   ├── StudentController.java         ← 学生管理
│   └── AttendanceController.java      ← 考勤管理
├── service/                           ← 业务层（核心逻辑）
│   ├── UserService.java               ← 用户注册/登录
│   ├── StudentService.java            ← 学生增删改查 + Excel导入
│   ├── AttendanceService.java         ← 考勤打卡 + 统计
│   ├── CourseService.java             ← 课程查询
│   └── security/
│       └── CustomUserDetailsService.java  ← Spring Security 用户认证
├── repository/                        ← 数据访问层（操作数据库）
│   ├── UserRepository.java
│   ├── StudentRepository.java
│   ├── AttendanceRepository.java
│   └── CourseRepository.java
├── entity/                            ← 实体类（对应数据库表）
│   ├── User.java                      ← 用户表 (users)
│   ├── Student.java                   ← 学生表 (student)
│   ├── Attendance.java                ← 考勤记录表 (attendance)
│   ├── Course.java                    ← 课程表 (course)
│   └── Role.java                      ← 用户角色枚举
├── dto/                               ← 数据传输对象
├── exception/                         ← 全局异常处理
│   └── GlobalExceptionHandler.java
└── util/                              ← 工具类
    ├── Result.java                    ← 统一 JSON 响应
    └── ExcelUtils.java                ← Excel 解析工具
```

### 请求处理流程（重要！）

```
浏览器请求
    ↓
Controller（控制层）—— 接收请求参数，决定返回哪个页面
    ↓
Service（业务层）   —— 写业务逻辑（如签到时间校验、统计计算）
    ↓
Repository（数据层）—— 操作数据库（增删改查）
    ↓
数据库
```

---

## 📦 核心实体关系

| 实体 | 数据库表 | 说明 |
|------|---------|------|
| **User** | `users` | 登录账号（username, password, role） |
| **Student** | `student` | 学生信息（学号、姓名、班级、学院等） |
| **Attendance** | `attendance` | 考勤记录（谁+哪门课+哪天+状态） |
| **Course** | `course` | 课程信息（课程名、上课时间） |

**关键关系**：一个学生可以有多条考勤记录（每门课每天一条），一条考勤记录对应一门课程。

---

## 🧭 核心页面路由

| 路由 | 功能 | 登录要求 |
|------|------|---------|
| `/login` | 登录页面 | ❌ |
| `/register` | 注册页面 | ❌ |
| `/home` | 首页仪表盘 | ✅ |
| `/attendance/checkIn` | 签到/签退页面 | ✅ |
| `/attendance/list` | 考勤记录列表（可筛选） | ✅ |
| `/attendance/statistics` | 个人考勤统计（按周/月） | ✅ |
| `/attendance/statistics/class` | 班级考勤统计 | ✅ |
| `/attendance/import` | 批量导入考勤记录 | ✅ |
| `/attendance/export` | 导出考勤 CSV | ✅ |
| `/student/list` | 学生列表 | ✅ |
| `/student/add` | 新增学生 | ✅ |
| `/student/import` | Excel 导入学生 | ✅ |

---

## 🔐 安全机制

- **表单登录**：Spring Security 默认实现，走 `/doLogin` 路径
- **CSRF 保护**：POST 请求需要携带 CSRF 令牌（Thymeleaf 的 `th:action` 会自动添加）
- **密码加密**：BCrypt 加密存储，绝不存明文
- **路径保护**：未登录用户只能访问 `/login`、`/register` 和静态资源

---

## 📖 给初学者的学习建议

### 推荐阅读顺序

1. **`AttendanceSystemApplication.java`** —— 入口，看看 `@SpringBootApplication` 是什么
2. **`controller/` 下的文件** —— 理解请求如何进入后端
3. **`entity/` 下的文件** —— 理解数据模型
4. **`repository/` 下的文件** —— 理解数据库操作（重点看方法命名规则）
5. **`service/` 下的文件** —— 理解业务逻辑怎么写
6. **`application.properties`** —— 看看项目配置了什么

### 常见疑问

**Q：为什么改了代码没生效？**
A：Spring Boot 需要重启才能加载新代码。开发时可以安装"DevTools"实现热重载。

**Q：`@Autowired` 是什么？**
A：告诉 Spring 帮我自动创建一个对象并赋值给这个变量，不需要手动 `new`。

**Q：为什么有些接口用 `@Controller` 有些用 `@RestController`？**
A：`@Controller` 返回视图（HTML页面），`@RestController` 返回 JSON 数据。本项目大部分都返回 HTML 页面，所以用 `@Controller`。

**Q：JPA 的 `save()` 什么时候 INSERT 什么时候 UPDATE？**
A：实体主键（id）为 null 时 INSERT，不为 null 时 UPDATE。Spring Data JPA 会自动判断。
