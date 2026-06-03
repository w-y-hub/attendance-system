package com.example.attendance.repository;

import com.example.attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long>, JpaSpecificationExecutor<Attendance> {

    Optional<Attendance> findByStudentNoAndCourseIdAndAttendanceDate(String studentNo, Long courseId, LocalDate attendanceDate);

    List<Attendance> findByStudentNoAndAttendanceDateBetween(String studentNo, LocalDate startDate, LocalDate endDate);

    long countByStudentNoAndAttendanceDateBetween(String studentNo, LocalDate startDate, LocalDate endDate);

    long countByStudentNoAndAttendanceDateBetweenAndStatus(String studentNo, LocalDate startDate, LocalDate endDate, String status);

    long countByStudentNoAndAttendanceDateBetweenAndStatusIn(String studentNo, LocalDate startDate, LocalDate endDate, Collection<String> statuses);

    // ========== 按课程查询（教师用） ==========

    List<Attendance> findByCourseIdAndAttendanceDateBetween(Long courseId, LocalDate startDate, LocalDate endDate);

    long countByCourseIdAndAttendanceDateBetween(Long courseId, LocalDate startDate, LocalDate endDate);

    long countByCourseIdAndAttendanceDateBetweenAndStatusIn(Long courseId, LocalDate startDate, LocalDate endDate, Collection<String> statuses);

    long countByCourseIdAndAttendanceDateBetweenAndStatus(Long courseId, LocalDate startDate, LocalDate endDate, String status);

    // ========== 班级级统计 ==========

    List<Attendance> findByClassNameAndAttendanceDateBetween(String className, LocalDate startDate, LocalDate endDate);

    long countByClassNameAndAttendanceDateBetween(String className, LocalDate startDate, LocalDate endDate);

    long countByClassNameAndAttendanceDateBetweenAndStatus(String className, LocalDate startDate, LocalDate endDate, String status);

    long countByClassNameAndAttendanceDateBetweenAndStatusIn(String className, LocalDate startDate, LocalDate endDate, Collection<String> statuses);

    @Query("SELECT DISTINCT a.className FROM Attendance a WHERE a.className IS NOT NULL")
    List<String> findDistinctClassNameBy();
}