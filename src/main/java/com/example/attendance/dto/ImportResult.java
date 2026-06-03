package com.example.attendance.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 导入结果 —— 记录 Excel 批量导入的统计信息
 *
 * 【为什么需要 DTO（数据传输对象）？】
 * 实体（Entity）对应数据库表结构，不适合直接传给前端。
 * DTO 专门用来在不同层之间传递数据，
 * 只包含当前场景需要的数据。
 *
 * totalCount = 一共多少条
 * successCount = 成功了多少条
 * failCount = 失败了多少条（自动计算：total - success）
 * errorMessages = 每一条失败的具体原因
 */

public class ImportResult {

    private int totalCount;           // 总记录数
    private int successCount;         // 成功数
    private int failCount;            // 失败数（自动计算）
    private List<String> errorMessages = new ArrayList<>();  // 错误详情
    private String failReportFileName; // 失败报告文件名（预留，暂未使用）

    public ImportResult() {}

    // --- Getter / Setter ---
    public int getTotalCount() { return totalCount; }
    public int getSuccessCount() { return successCount; }
    public int getFailCount() { return failCount; }
    public List<String> getErrorMessages() { return errorMessages; }
    public String getFailReportFileName() { return failReportFileName; }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
        updateFailCount();
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
        updateFailCount();
    }

    public void setFailCount(int failCount) { this.failCount = failCount; }

    public void setErrorMessages(List<String> errorMessages) {
        this.errorMessages = errorMessages;
        updateFailCount();
    }

    public void setFailReportFileName(String failReportFileName) {
        this.failReportFileName = failReportFileName;
    }

    /** 添加一条错误信息，同时更新失败计数 */
    public void addErrorMessage(String message) {
        this.errorMessages.add(message);
        updateFailCount();
    }

    /** 自动计算失败数 = 总数 - 成功数（最低为0） */
    private void updateFailCount() {
        this.failCount = this.totalCount - this.successCount;
        if (this.failCount < 0) {
            this.failCount = 0;
        }
    }
}