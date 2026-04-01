-- =============================================================
-- 第一步：清理旧数据（可选，注意顺序，先删从表再删主表）
-- =============================================================
TRUNCATE TABLE attendance, course_selection, course, "user" RESTART IDENTITY CASCADE;

-- =============================================================
-- 第二步：向 user 表插入测试数据
-- =============================================================
-- 注意：id 是自增的，所以不需要手动插入 id
INSERT INTO "user" (username, password, real_name, role)
VALUES ('admin', 'admin123', '系统管理员', 'ADMIN'),
       ('zhang_teacher', '123456', '张三', 'TEACHER'),
       ('li_teacher', '123456', '李四', 'TEACHER');

-- =============================================================
-- 第三步：向 course 表插入测试数据
-- =============================================================
-- 假设 zhang_teacher 的 id 是 2
INSERT INTO course (course_id, course_name, class_name, teacher_id,
                    classroom_name, rows, cols, exclude_seats,
                    weekday, start_week, end_week)
VALUES ('CS101', 'SpringBoot开发实践', '21级计科1班', 2,
        '通博楼B302', 8, 10, '1,1;1,2;8,10',
        1, 1, 16),
       ('CS102', '数据库系统基础', '21级信管1班', 2,
        '颐德楼H101', 5, 6, NULL,
        3, 1, 16);

-- =============================================================
-- 第四步：向 course_selection 表插入 5 条选课数据
-- =============================================================
INSERT INTO course_selection (student_id, student_name, gender, course_id)
VALUES ('20230001', '张小明', '男', 'CS101'),
       ('20230002', '王小红', '女', 'CS101'),
       ('20230003', '李雷', '男', 'CS101'),
       ('20230004', '韩梅梅', '女', 'CS101'),
       ('20230005', '赵铁柱', '男', 'CS101');

-- =============================================================
-- 第五步：向 attendance 表插入若干考勤记录
-- =============================================================
INSERT INTO attendance (student_id, student_name, course_id,
                        check_in_time, seat_row, seat_col, status, ip)
VALUES
-- 正常签到
('20230001', '张小明', 'CS101', '2023-10-30 08:55:00', 2, 3, 'NORMAL', '192.168.1.101'),
('20230002', '王小红', 'CS101', '2023-10-30 08:58:20', 2, 4, 'NORMAL', '192.168.1.102'),
-- 迟到
('20230003', '李雷', 'CS101', '2023-10-30 09:15:00', 3, 1, 'LATE', '192.168.1.105'),
-- 另一个学生正常签到
('20230004', '韩梅梅', 'CS101', '2023-10-30 08:50:10', 1, 5, 'NORMAL', '192.168.1.110');

-- =============================================================
-- 验证数据查询
-- =============================================================
SELECT '用户总数' as info, COUNT(*)
FROM "user"
UNION ALL
SELECT '课程总数', COUNT(*)
FROM course
UNION ALL
SELECT '选课总数', COUNT(*)
FROM course_selection
UNION ALL
SELECT '考勤记录', COUNT(*)
FROM attendance;

INSERT INTO "user"(username, password, real_name, role)
VALUES ('liu_student', 123456, '刘备', 'STUDENT'),
       ('guan_student', 123456, '关羽', 'STUDENT'),
       ('zhang_student', 123456, '张飞', 'STUDENT');