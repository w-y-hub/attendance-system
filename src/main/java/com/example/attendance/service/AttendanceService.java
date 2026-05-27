package com.example.attendance.service;

import com.example.attendance.dto.AttendanceStatisticsItemDto;
import com.example.attendance.dto.AttendanceStatisticsPageDto;
import com.example.attendance.entity.Attendance;
import com.example.attendance.repository.AttendanceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;

    public AttendanceService(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    // 出勤状态：正常 + 迟到 + 早退 都算出勤
    private static final List<String> PRESENT_STATUSES = Arrays.asList("NORMAL", "LATE", "EARLY");

    // 缺勤状态
    private static final String ABSENT_STATUS = "ABSENT";

    public Attendance save(Attendance attendance) {
        return attendanceRepository.save(attendance);
    }

    public Optional<Attendance> findByStudentNoAndCourseIdAndAttendanceDate(String studentNo, Long courseId, LocalDate attendanceDate) {
        return attendanceRepository.findByStudentNoAndCourseIdAndAttendanceDate(studentNo, courseId, attendanceDate);
    }

    public Page<Attendance> findAll(Specification<Attendance> spec, Pageable pageable) {
        return attendanceRepository.findAll(spec, pageable);
    }

    public List<Attendance> findAll(Specification<Attendance> spec) {
        return attendanceRepository.findAll(spec);
    }

    /**
     * 获取某学生的统计页面数据
     */
    public AttendanceStatisticsPageDto getAttendanceStatistics(String studentNo, LocalDate startDate, LocalDate endDate) {
        AttendanceStatisticsPageDto pageDto = new AttendanceStatisticsPageDto();

        if (studentNo == null || studentNo.trim().isEmpty()) {
            return pageDto;
        }

        if (startDate == null || endDate == null) {
            return pageDto;
        }

        if (startDate.isAfter(endDate)) {
            return pageDto;
        }

        pageDto.setStartDate(startDate.toString());
        pageDto.setEndDate(endDate.toString());

        long totalCount = attendanceRepository.countByStudentNoAndAttendanceDateBetween(studentNo, startDate, endDate);
        long presentCount = attendanceRepository.countByStudentNoAndAttendanceDateBetweenAndStatusIn(
                studentNo, startDate, endDate, PRESENT_STATUSES
        );
        long absentCount = attendanceRepository.countByStudentNoAndAttendanceDateBetweenAndStatus(
                studentNo, startDate, endDate, ABSENT_STATUS
        );

        pageDto.setRangeTotalCount(totalCount);
        pageDto.setRangePresentCount(presentCount);
        pageDto.setRangeAbsentCount(absentCount);
        pageDto.setRangeAttendanceRate(calculateRate(presentCount, totalCount));

        pageDto.setWeeklyStatistics(buildWeeklyStatistics(studentNo, startDate, endDate));
        pageDto.setMonthlyStatistics(buildMonthlyStatistics(studentNo, startDate, endDate));

        return pageDto;
    }

    private List<AttendanceStatisticsItemDto> buildWeeklyStatistics(String studentNo, LocalDate startDate, LocalDate endDate) {
        List<AttendanceStatisticsItemDto> list = new ArrayList<>();

        LocalDate currentStart = startDate;
        while (!currentStart.isAfter(endDate)) {
            LocalDate currentEnd = currentStart.plusDays(6);
            if (currentEnd.isAfter(endDate)) {
                currentEnd = endDate;
            }

            AttendanceStatisticsItemDto item = buildStatisticsItem(
                    studentNo,
                    currentStart,
                    currentEnd,
                    currentStart + " ~ " + currentEnd
            );

            list.add(item);
            currentStart = currentEnd.plusDays(1);
        }

        return list;
    }

    private List<AttendanceStatisticsItemDto> buildMonthlyStatistics(String studentNo, LocalDate startDate, LocalDate endDate) {
        List<AttendanceStatisticsItemDto> list = new ArrayList<>();

        YearMonth currentMonth = YearMonth.from(startDate);
        YearMonth endMonth = YearMonth.from(endDate);

        while (!currentMonth.isAfter(endMonth)) {
            LocalDate monthStart = currentMonth.atDay(1);
            LocalDate monthEnd = currentMonth.atEndOfMonth();

            if (monthStart.isBefore(startDate)) {
                monthStart = startDate;
            }
            if (monthEnd.isAfter(endDate)) {
                monthEnd = endDate;
            }

            AttendanceStatisticsItemDto item = buildStatisticsItem(
                    studentNo,
                    monthStart,
                    monthEnd,
                    currentMonth.toString()
            );

            list.add(item);
            currentMonth = currentMonth.plusMonths(1);
        }

        return list;
    }

    private AttendanceStatisticsItemDto buildStatisticsItem(String studentNo, LocalDate startDate, LocalDate endDate, String label) {
        long totalCount = attendanceRepository.countByStudentNoAndAttendanceDateBetween(studentNo, startDate, endDate);
        long presentCount = attendanceRepository.countByStudentNoAndAttendanceDateBetweenAndStatusIn(
                studentNo, startDate, endDate, PRESENT_STATUSES
        );
        long absentCount = attendanceRepository.countByStudentNoAndAttendanceDateBetweenAndStatus(
                studentNo, startDate, endDate, ABSENT_STATUS
        );

        AttendanceStatisticsItemDto item = new AttendanceStatisticsItemDto();
        item.setLabel(label);
        item.setTotalCount(totalCount);
        item.setPresentCount(presentCount);
        item.setAbsentCount(absentCount);
        item.setAttendanceRate(calculateRate(presentCount, totalCount));

        return item;
    }

    private double calculateRate(long presentCount, long totalCount) {
        if (totalCount == 0) {
            return 0.0;
        }
        return Math.round(presentCount * 10000.0 / totalCount) / 100.0;
    }
}