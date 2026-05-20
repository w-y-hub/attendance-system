package com.example.attendance.service;

import com.example.attendance.entity.Attendance;
import com.example.attendance.repository.AttendanceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;

    public AttendanceService(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    public Attendance save(Attendance attendance) {
        return attendanceRepository.save(attendance);
    }

    public Page<Attendance> findAll(Specification<Attendance> spec, Pageable pageable) {
        return attendanceRepository.findAll(spec, pageable);
    }

    public List<Attendance> findAll(Specification<Attendance> spec) {
        return attendanceRepository.findAll(spec);
    }

    public Optional<Attendance> findByStudentNoAndCourseIdAndAttendanceDate(String studentNo, Long courseId, LocalDate attendanceDate) {
        return attendanceRepository.findByStudentNoAndCourseIdAndAttendanceDate(studentNo, courseId, attendanceDate);
    }
}