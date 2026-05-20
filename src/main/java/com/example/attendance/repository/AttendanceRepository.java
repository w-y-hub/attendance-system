package com.example.attendance.repository;

import com.example.attendance.entity.Attendance;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long>, JpaSpecificationExecutor<Attendance> {

    Optional<Attendance> findByStudentNoAndCourseIdAndAttendanceDate(String studentNo, Long courseId, LocalDate attendanceDate);
}