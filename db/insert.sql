-- ====================================================================
-- 测试数据插入脚本
-- 运行前提：表结构已由 JPA 自动创建（spring.jpa.hibernate.ddl-auto=update）
-- ====================================================================

-- 1. 插入测试用户（密码 123456 的 BCrypt 加密结果）
-- 实际注册时密码由后端自动加密，所以这里直接插密文
INSERT INTO users (username, password, role, enabled)
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', true),
       ('teacher1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'TEACHER', true),
       ('42211077', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'STUDENT', true);

-- 2. 插入测试学生
INSERT INTO student (student_no, name, gender, college, grade, major, class_name, status)
VALUES ('42211077', '孙涛涛', '男', '计算机与人工智能学院', '2022', '计算机科学与技术', '2022级计算机科学与技术', 1),
       ('42211078', '李小明', '男', '计算机与人工智能学院', '2022', '计算机科学与技术', '2022级计算机科学与技术', 1),
       ('42211079', '王小红', '女', '计算机与人工智能学院', '2022', '计算机科学与技术', '2022级计算机科学与技术', 1);

-- 3. 插入测试课程
INSERT INTO course (course_name, class_name, start_time, end_time)
VALUES ('JavaEE开发实践', '2022级计算机科学与技术', '08:30:00', '10:00:00'),
       ('数据库系统原理', '2022级计算机科学与技术', '10:15:00', '11:45:00');