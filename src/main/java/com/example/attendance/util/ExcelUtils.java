package com.example.attendance.util;

import org.apache.poi.ss.usermodel.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Excel 单元格读取工具类
 */
public class ExcelUtils {

    /** 支持的日期格式列表（按优先级） */
    private static final List<DateTimeFormatter> DATE_FORMATTERS = Arrays.asList(
            DateTimeFormatter.ISO_LOCAL_DATE,                    // 2025-05-20
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),           // 2025/05/20
            DateTimeFormatter.ofPattern("yyyyMMdd"),              // 20250520
            DateTimeFormatter.ofPattern("yyyy.MM.dd"),           // 2025.05.20
            DateTimeFormatter.ofPattern("yyyy年MM月dd日")        // 2025年05月20日
    );

    /**
     * 读取单元格内容，并统一转成字符串
     */
    public static String getCellStringValue(Cell cell) {
        if (cell == null) {
            return "";
        }

        CellType cellType = cell.getCellType();

        switch (cellType) {
            case STRING:
                return cell.getStringCellValue().trim();

            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    Date date = cell.getDateCellValue();
                    LocalDate localDate = date.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    return localDate.toString();
                } else {
                    double value = cell.getNumericCellValue();
                    long longValue = (long) value;
                    if (value == longValue) {
                        return String.valueOf(longValue);
                    }
                    return String.valueOf(value);
                }

            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());

            case FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (Exception e) {
                    try {
                        double value = cell.getNumericCellValue();
                        long longValue = (long) value;
                        if (value == longValue) {
                            return String.valueOf(longValue);
                        }
                        return String.valueOf(value);
                    } catch (Exception ex) {
                        return "";
                    }
                }

            case BLANK:
            default:
                return "";
        }
    }

    /**
     * 读取 Excel 日期单元格并转为 LocalDate
     */
    public static LocalDate getCellLocalDateValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                Date date = cell.getDateCellValue();
                return date.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
            }

            String text = getCellStringValue(cell);
            if (text == null || text.trim().isEmpty()) {
                return null;
            }

            return parseDateFlexible(text.trim());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 灵活解析日期字符串，支持多种格式
     *
     * 【重要说明】
     * 这里不抛异常，而是返回 null。为什么？
     * 调用方通常只需要知道"能不能解析"，
     * 如果抛异常，调用方就得 try-catch，代码啰嗦。
     * 返回 null 后，调用方直接 if (result == null) 处理即可。
     *
     * @param dateStr 日期字符串
     * @return 解析成功返回 LocalDate，失败返回 null
     */
    public static LocalDate parseDateFlexible(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        String trimmed = dateStr.trim();

        // 依次尝试每个日期格式，成功就返回
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(trimmed, formatter);
            } catch (DateTimeParseException ignored) {
                // 这个格式不行，试下一个
            }
        }

        // 所有格式都解析失败，返回 null 而不是抛异常
        return null;
    }
}