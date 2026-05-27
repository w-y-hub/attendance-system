package com.example.attendance.util;

import org.apache.poi.ss.usermodel.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * 这是在干什么：
 * 这个工具类用于统一处理 Excel 单元格读取。
 *
 * 如何实现：
 * Apache POI 的单元格类型可能是字符串、数字、日期、布尔值等，
 * 我们在这里封装通用读取逻辑，避免在业务代码里写很多重复判断。
 */
public class ExcelUtils {

    /**
     * 这是在干什么：
     * 读取单元格内容，并统一转成字符串。
     *
     * 如何实现：
     * 根据单元格类型做不同处理：
     * - STRING：直接读字符串
     * - NUMERIC：判断是否为日期，否则转成数字字符串
     * - BOOLEAN：转成 true/false
     * - FORMULA：优先按公式结果读取
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
                    // 防止学号、手机号被读成 1.0 这种格式
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
     * 这是在干什么：
     * 读取 Excel 日期单元格并转为 LocalDate。
     *
     * 如何实现：
     * 如果单元格本身是 Excel 日期格式，就直接转换；
     * 如果是字符串，就尝试用 LocalDate.parse 解析。
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

            return LocalDate.parse(text.trim());
        } catch (Exception e) {
            return null;
        }
    }
}