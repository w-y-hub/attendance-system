package com.example.attendance.dto;

public class AttendanceStatisticsItemDto {

    // 例如：2025-05-01 ~ 2025-05-07，或者 2025-05
    private String label;

    // 该区间总考勤记录数
    private long totalCount;

    // 出勤次数（NORMAL + LATE）
    private long presentCount;

    // 缺勤次数（ABSENT）
    private long absentCount;

    // 出勤率
    private double attendanceRate;

    public AttendanceStatisticsItemDto() {
    }

    public AttendanceStatisticsItemDto(String label, long totalCount, long presentCount, long absentCount, double attendanceRate) {
        this.label = label;
        this.totalCount = totalCount;
        this.presentCount = presentCount;
        this.absentCount = absentCount;
        this.attendanceRate = attendanceRate;
    }

    public String getLabel() {
        return label;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public long getPresentCount() {
        return presentCount;
    }

    public long getAbsentCount() {
        return absentCount;
    }

    public double getAttendanceRate() {
        return attendanceRate;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public void setPresentCount(long presentCount) {
        this.presentCount = presentCount;
    }

    public void setAbsentCount(long absentCount) {
        this.absentCount = absentCount;
    }

    public void setAttendanceRate(double attendanceRate) {
        this.attendanceRate = attendanceRate;
    }
}