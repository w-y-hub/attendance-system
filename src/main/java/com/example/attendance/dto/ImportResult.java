package com.example.attendance.dto;

import java.util.ArrayList;
import java.util.List;

public class ImportResult {

    private int totalCount;
    private int successCount;
    private int failCount;
    private List<String> errorMessages = new ArrayList<>();

    // 失败报告下载用
    private String failReportFileName;

    public ImportResult() {
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
        updateFailCount();
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
        updateFailCount();
    }

    public int getFailCount() {
        return failCount;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(List<String> errorMessages) {
        this.errorMessages = errorMessages;
        updateFailCount();
    }

    public String getFailReportFileName() {
        return failReportFileName;
    }

    public void setFailReportFileName(String failReportFileName) {
        this.failReportFileName = failReportFileName;
    }

    public void addErrorMessage(String message) {
        this.errorMessages.add(message);
        updateFailCount();
    }

    private void updateFailCount() {
        this.failCount = this.totalCount - this.successCount;
        if (this.failCount < 0) {
            this.failCount = 0;
        }
    }
}