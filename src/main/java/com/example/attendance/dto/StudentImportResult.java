package com.example.attendance.dto;

/**
 * 学生导入结果 —— 继承 ImportResult，暂时没有额外字段
 *
 * 【为什么要有这个类？】
 * 虽然现在和 ImportResult 一模一样，但以后可能学生导入需要特殊的统计信息
 * （比如：成功导入的班级分布、重复学号数量等），
 * 直接在 StudentImportResult 中加字段，不影响考勤导入的逻辑。
 *
 * 【继承】
 * StudentImportResult IS-A ImportResult，
 * 继承了 totalCount、successCount、errorMessages 等所有字段和方法。
 */

public class StudentImportResult extends ImportResult {

    public StudentImportResult() {
        super();
    }
}