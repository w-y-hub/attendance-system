package com.example.attendance.repository;

import com.example.attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long>, JpaSpecificationExecutor<Attendance> {

    Optional<Attendance> findByStudentNoAndCourseIdAndAttendanceDate(String studentNo, Long courseId, LocalDate attendanceDate);

    // 查询某学生在某日期范围内的考勤记录
    List<Attendance> findByStudentNoAndAttendanceDateBetween(String studentNo, LocalDate startDate, LocalDate endDate);

    // 统计某学生在某日期范围内的总记录数
    long countByStudentNoAndAttendanceDateBetween(String studentNo, LocalDate startDate, LocalDate endDate);

    // 统计某学生在某日期范围内、单个状态的记录数
    long countByStudentNoAndAttendanceDateBetweenAndStatus(String studentNo, LocalDate startDate, LocalDate endDate, String status);

    // 统计某学生在某日期范围内、多个状态的记录数
    long countByStudentNoAndAttendanceDateBetweenAndStatusIn(String studentNo, LocalDate startDate, LocalDate endDate, Collection<String> statuses);
}