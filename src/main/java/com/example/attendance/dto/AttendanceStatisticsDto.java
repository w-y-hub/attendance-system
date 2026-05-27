package com.example.attendance.dto;

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