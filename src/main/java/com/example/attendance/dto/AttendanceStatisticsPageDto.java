package com.example.attendance.dto;

import java.util.ArrayList;
import java.util.List;

public class AttendanceStatisticsPageDto {

    private String startDate;
    private String endDate;

    // 整个日期范围汇总
    private long rangeTotalCount;
    private long rangePresentCount;
    private long rangeAbsentCount;
    private double rangeAttendanceRate;

    // 按周统计
    private List<AttendanceStatisticsItemDto> weeklyStatistics = new ArrayList<>();

    // 按月统计
    private List<AttendanceStatisticsItemDto> monthlyStatistics = new ArrayList<>();

    public AttendanceStatisticsPageDto() {
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public long getRangeTotalCount() {
        return rangeTotalCount;
    }

    public long getRangePresentCount() {
        return rangePresentCount;
    }

    public long getRangeAbsentCount() {
        return rangeAbsentCount;
    }

    public double getRangeAttendanceRate() {
        return rangeAttendanceRate;
    }

    public List<AttendanceStatisticsItemDto> getWeeklyStatistics() {
        return weeklyStatistics;
    }

    public List<AttendanceStatisticsItemDto> getMonthlyStatistics() {
        return monthlyStatistics;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public void setRangeTotalCount(long rangeTotalCount) {
        this.rangeTotalCount = rangeTotalCount;
    }

    public void setRangePresentCount(long rangePresentCount) {
        this.rangePresentCount = rangePresentCount;
    }

    public void setRangeAbsentCount(long rangeAbsentCount) {
        this.rangeAbsentCount = rangeAbsentCount;
    }

    public void setRangeAttendanceRate(double rangeAttendanceRate) {
        this.rangeAttendanceRate = rangeAttendanceRate;
    }

    public void setWeeklyStatistics(List<AttendanceStatisticsItemDto> weeklyStatistics) {
        this.weeklyStatistics = weeklyStatistics;
    }

    public void setMonthlyStatistics(List<AttendanceStatisticsItemDto> monthlyStatistics) {
        this.monthlyStatistics = monthlyStatistics;
    }
}