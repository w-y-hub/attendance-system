package com.example.attendance.dto;

/**
 * 考勤统计项 —— 单个统计区间的数据
 *
 * 无论按周还是按月，每一段统计都是同样的结构：
 *   label          → 区间名称（如 "2025-05" 或 "05-01 ~ 05-07"）
 *   totalCount     → 该区间总考勤次数
 *   presentCount   → 出勤次数（NORMAL + LATE + EARLY）
 *   absentCount    → 缺勤次数（ABSENT）
 *   attendanceRate → 出勤率（百分比，保留两位小数）
 */

public class AttendanceStatisticsDto {

    private String label;
    private long totalCount;
    private long presentCount;
    private double attendanceRate;

    public AttendanceStatisticsDto() {
    }

    public AttendanceStatisticsDto(String label, long totalCount, long presentCount, double attendanceRate) {
        this.label = label;
        this.totalCount = totalCount;
        this.presentCount = presentCount;
        this.attendanceRate = attendanceRate;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public long getPresentCount() {
        return presentCount;
    }

    public void setPresentCount(long presentCount) {
        this.presentCount = presentCount;
    }

    public double getAttendanceRate() {
        return attendanceRate;
    }

    public void setAttendanceRate(double attendanceRate) {
        this.attendanceRate = attendanceRate;
    }
}