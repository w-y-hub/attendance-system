package com.example.attendance.service;

import com.example.attendance.dto.AttendanceStatisticsItemDto;
import com.example.attendance.dto.AttendanceStatisticsPageDto;
import com.example.attendance.dto.ImportResult;
import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.Course;
import com.example.attendance.entity.Student;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.repository.CourseRepository;
import com.example.attendance.repository.StudentRepository;
import com.example.attendance.util.ExcelUtils;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class AttendanceService {

    /**
     * 考勤业务层 —— 核心逻辑都在这
     *
     * 包含的功能：
     *   保存考勤记录（签到/签退）
     *   按条件查询（Specification 动态查询）
     *   Excel 批量导入（parseAndSaveRow）
     *   个人统计（按周/按月）
     *   班级统计（所有班级的出勤率对比）
     *
     * 【Logger（日志）】
     * private static final Logger log = LoggerFactory.getLogger(...)
     * 这是 SLF4J 日志框架的标准写法。
     * 用 log.info() / log.warn() / log.error() 代替 System.out.println()，
     * 因为日志可以控制级别、输出到文件、方便排查线上问题。
     */

    private static final Logger log = LoggerFactory.getLogger(AttendanceService.class);

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** 合法的考勤状态列表（用于校验导入数据） */
    private static final List<String> VALID_STATUSES = Arrays.asList("NORMAL", "LATE", "EARLY", "ABSENT");

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             StudentRepository studentRepository,
                             CourseRepository courseRepository) {
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    private static final List<String> PRESENT_STATUSES = Arrays.asList("NORMAL", "LATE", "EARLY");

    private static final String ABSENT_STATUS = "ABSENT";

    /**
     * 保存一条考勤记录
     *
     * save() 是 JPA 提供的"万能保存"方法：
     * - 如果 attendance.id == null → 执行 INSERT
     * - 如果 attendance.id != null → 执行 UPDATE
     *
     * @param attendance 考勤记录对象
     * @return 保存后的考勤记录（含自增 id）
     */
    public Attendance save(Attendance attendance) {
        return attendanceRepository.save(attendance);
    }

    /**
     * 学生签到 —— 核心业务逻辑
     *
     * 【分层规范】
     * 签到的时间校验、重复检查、状态判定都是"业务逻辑"，
     * 应该放在 Service 层，而不是 Controller。
     * Controller 只负责"接收参数、返回页面"。
     *
     * 【业务规则】
     * 1. 学生和课程必须存在
     * 2. 签到时间必须在"课前15分钟 ~ 上课后30分钟"内
     * 3. 同一学生同一课程同一天不能重复签到
     * 4. 签到时间晚于课程开始时间 → 状态=迟到（LATE）
     *    签到时间早于课程开始时间 → 状态=正常（NORMAL）
     *
     * @param student  要签到的学生
     * @param course   要签到的课程
     * @param remark   备注
     * @param checkInTime 签到时间
     * @return 签到结果消息（null = 成功，非null = 错误信息）
     */
    public String checkIn(Student student, Course course, String remark, LocalDateTime checkInTime) {
        // ---- 校验学生和课程 ----
        if (student == null) {
            return "当前学生不存在，打卡失败";
        }
        if (course == null) {
            return "课程不存在";
        }

        LocalDate today = checkInTime.toLocalDate();
        LocalTime nowTime = checkInTime.toLocalTime();

        // ---- 校验课程时间是否配置 ----
        LocalTime startTime = course.getStartTime();
        LocalTime endTime = course.getEndTime();
        if (startTime == null || endTime == null) {
            return "课程时间未配置完整";
        }

        // ---- 校验签到时间段 ----
        // 定向越野课程：课前30分钟到课后4小时内均可签到
        LocalTime allowedStart = startTime.minusMinutes(240);
        LocalTime allowedEnd = startTime.plusMinutes(240);
        if (nowTime.isBefore(allowedStart) || nowTime.isAfter(allowedEnd)) {
            return "当前不在允许签到时间内（课程开始前4小时到课后4小时）";
        }

        // ---- 检查重复签到 ----
        Optional<Attendance> existing = attendanceRepository
                .findByStudentNoAndCourseIdAndAttendanceDate(student.getStudentNo(), course.getId(), today);
        if (existing.isPresent()) {
            return "今天该课程已签到，请勿重复打卡";
        }

        // ---- 创建考勤记录 ----
        Attendance attendance = new Attendance();
        attendance.setStudentNo(student.getStudentNo());
        attendance.setStudentName(student.getName());
        attendance.setClassName(student.getClassName());
        attendance.setCourseId(course.getId());
        attendance.setAttendanceDate(today);
        attendance.setRemark(remark);
        attendance.setCreateTime(checkInTime);
        attendance.setUpdateTime(checkInTime);
        attendance.setCheckInTime(checkInTime);

        // 判定状态：上课后才签到 → 迟到
        if (nowTime.isAfter(startTime)) {
            attendance.setStatus("LATE");
        } else {
            attendance.setStatus("NORMAL");
        }
        attendance.setEarlyLeave(false);

        attendanceRepository.save(attendance);
        return null;  // null = 成功
    }

    /**
     * 签退 —— 核心业务逻辑
     *
     * 1. 必须先签到才能签退
     * 2. 不能重复签退
     * 3. 早于课程结束时间签退 → 标记为早退
     *
     * @param studentNo 学号
     * @param courseId  课程 ID
     * @param checkOutTime 签退时间
     * @return 签到结果消息（null = 成功，非null = 错误信息）
     */
    public String checkOut(String studentNo, Long courseId, LocalDateTime checkOutTime) {
        LocalDate today = checkOutTime.toLocalDate();

        // 查找今天的签到记录
        Optional<Attendance> optional = attendanceRepository
                .findByStudentNoAndCourseIdAndAttendanceDate(studentNo, courseId, today);
        if (optional.isEmpty()) {
            return "请先签到，再签退";
        }

        Attendance attendance = optional.get();

        // 不能重复签退
        if (attendance.getCheckOutTime() != null) {
            return "今天该课程已签退，请勿重复操作";
        }

        // 记录签退时间
        attendance.setCheckOutTime(checkOutTime);
        attendance.setUpdateTime(checkOutTime);

        // 判断是否早退
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course != null && course.getEndTime() != null
                && checkOutTime.toLocalTime().isBefore(course.getEndTime())) {
            attendance.setEarlyLeave(true);
            attendance.setStatus("EARLY");
        }

        attendanceRepository.save(attendance);
        return null;
    }

    /**
     * 查询某学生某课程某天的考勤记录
     *
     * 【Optional<Attendance> 返回值】
     * Optional 是"可能为空"的容器。
     * .isPresent() 判断是否存在，.get() 获取值。
     * 用 Optional 比返回 null 更安全——强迫调用方考虑"没查到"的情况。
     *
     * @param studentNo      学号
     * @param courseId       课程 ID
     * @param attendanceDate 考勤日期
     * @return 可能包含考勤记录的 Optional
     */
    public Optional<Attendance> findByStudentNoAndCourseIdAndAttendanceDate(String studentNo, Long courseId, LocalDate attendanceDate) {
        return attendanceRepository.findByStudentNoAndCourseIdAndAttendanceDate(studentNo, courseId, attendanceDate);
    }

    /**
     * 查询某学生某天的所有考勤记录（用于签退面板）
     *
     * @param studentNo 学号
     * @param date      日期
     * @return 当天的考勤记录列表（可能为空）
     */
    public List<Attendance> findByStudentNoAndDate(String studentNo, LocalDate date) {
        return attendanceRepository.findByStudentNoAndAttendanceDateBetween(studentNo, date, date);
    }

    /**
     * 查询某课程在日期范围内的考勤记录（教师查看班级考勤用）
     *
     * @param courseId  课程 ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 考勤记录列表
     */
    public List<Attendance> findByCourseIdAndDateRange(Long courseId, LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findByCourseIdAndAttendanceDateBetween(courseId, startDate, endDate);
    }

    /**
     * 按条件分页查询考勤记录
     *
     * 【Specification】
     * JPA 的动态查询接口，可以在运行时组合 WHERE 条件。
     * 例如：spec.and(cb.equal(root.get("status"), "LATE"))
     *
     * 【Pageable】
     * 分页参数，包含页码（从0开始）和每页条数。
     * PageRequest.of(0, 10) = 第1页，每页10条。
     *
     * @param spec     查询条件
     * @param pageable 分页参数
     * @return 分页结果（含总条数、当前页数据等）
     */
    public Page<Attendance> findAll(Specification<Attendance> spec, Pageable pageable) {
        return attendanceRepository.findAll(spec, pageable);
    }

    /**
     * 按条件查询所有考勤记录（不分页，用于导出 CSV）
     *
     * @param spec 查询条件
     * @return 符合条件的全部考勤记录列表
     */
    public List<Attendance> findAll(Specification<Attendance> spec) {
        return attendanceRepository.findAll(spec);
    }

    /**
     * 根据主键 ID 查询考勤记录
     *
     * @param id 考勤记录 ID
     * @return Optional，可能为空
     */
    public Optional<Attendance> findById(Long id) {
        return attendanceRepository.findById(id);
    }

    /**
     * 根据主键 ID 删除考勤记录
     *
     * @param id 要删除的考勤记录 ID
     */
    public void deleteById(Long id) {
        attendanceRepository.deleteById(id);
    }

    /**
     * 从上传的 Excel 文件批量导入考勤记录
     *
     * 【Excel 列顺序】
     *   列0：学号       （必填，必须在 student 表中存在）
     *   列1：课程名称    （必填，必须在 course 表中存在）
     *   列2：考勤日期    （必填，支持 yyyy-MM-dd 等多种格式）
     *   列3：签到时间    （可选，格式 yyyy-MM-dd HH:mm:ss）
     *   列4：状态        （可选，NORMAL/LATE/EARLY/ABSENT，默认 NORMAL）
     *   列5：备注        （可选）
     *
     * 【校验规则】
     *   - 每行先检查是否是空行（isEmptyRow）
     *   - 再对必填字段做非空校验
     *   - 验证学生是否存在、课程是否存在
     *   - 验证同一学生同一课程同一天不重复
     *   - 验证状态值是否合法
     *
     * @param file 上传的 Excel 文件
     * @return 导入结果（总数、成功数、失败数、错误详情）
     */
    public ImportResult importFromExcel(MultipartFile file) {
        ImportResult result = new ImportResult();

        if (file == null || file.isEmpty()) {
            result.addErrorMessage("上传文件不能为空");
            return result;
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !(fileName.toLowerCase().endsWith(".xlsx") || fileName.toLowerCase().endsWith(".xls"))) {
            result.addErrorMessage("请上传 .xlsx 或 .xls 格式的 Excel 文件");
            return result;
        }

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();

            if (lastRowNum < 1) {
                result.addErrorMessage("Excel 文件没有可导入的数据");
                return result;
            }

            for (int i = 1; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (isEmptyRow(row)) continue;

                result.setTotalCount(result.getTotalCount() + 1);

                try {
                    String errorMsg = parseAndSaveRow(row, i + 1);
                    if (errorMsg != null) {
                        result.addErrorMessage(errorMsg);
                    } else {
                        result.setSuccessCount(result.getSuccessCount() + 1);
                    }
                } catch (Exception e) {
                    log.error("解析第 {} 行失败：{}", i + 1, e.getMessage());
                    result.addErrorMessage("第 " + (i + 1) + " 行导入异常：" + e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("Excel 文件解析失败", e);
            result.addErrorMessage("Excel 文件解析失败：" + e.getMessage());
        }

        return result;
    }

    private boolean isEmptyRow(Row row) {
        if (row == null) return true;
        for (int i = 0; i <= 5; i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String val = ExcelUtils.getCellStringValue(cell);
                if (val != null && !val.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private String parseAndSaveRow(Row row, int rowNum) {
        String studentNo = ExcelUtils.getCellStringValue(row.getCell(0)).trim();
        String courseName = ExcelUtils.getCellStringValue(row.getCell(1)).trim();
        String dateStr = ExcelUtils.getCellStringValue(row.getCell(2)).trim();
        String checkInStr = ExcelUtils.getCellStringValue(row.getCell(3)).trim();
        String status = ExcelUtils.getCellStringValue(row.getCell(4)).trim().toUpperCase();
        String remark = ExcelUtils.getCellStringValue(row.getCell(5)).trim();

        if (studentNo.isEmpty()) {
            return "第 " + rowNum + " 行：学号不能为空";
        }
        if (courseName.isEmpty()) {
            return "第 " + rowNum + " 行：课程名称不能为空";
        }
        if (dateStr.isEmpty()) {
            return "第 " + rowNum + " 行：考勤日期不能为空";
        }

        LocalDate attendanceDate = ExcelUtils.parseDateFlexible(dateStr);
        if (attendanceDate == null) {
            return "第 " + rowNum + " 行：考勤日期格式不正确，当前值【" + dateStr + "】（请使用 yyyy-MM-dd 格式，如 2025-05-20）";
        }

        Student student = studentRepository.findByStudentNo(studentNo);
        if (student == null) {
            return "第 " + rowNum + " 行：学号 " + studentNo + " 在系统中不存在";
        }

        List<Course> courses = courseRepository.findByCourseName(courseName);
        if (courses.isEmpty()) {
            return "第 " + rowNum + " 行：课程「" + courseName + "」在系统中不存在";
        }
        if (courses.size() > 1) {
            return "第 " + rowNum + " 行：课程「" + courseName + "」存在多条同名记录，无法确定";
        }
        Long courseId = courses.get(0).getId();

        Optional<Attendance> existing = attendanceRepository
                .findByStudentNoAndCourseIdAndAttendanceDate(studentNo, courseId, attendanceDate);
        if (existing.isPresent()) {
            return "第 " + rowNum + " 行：该学生此课程当天已有考勤记录，不能重复导入";
        }

        LocalDateTime checkInTime = null;
        if (!checkInStr.isEmpty()) {
            try {
                checkInTime = LocalDateTime.parse(checkInStr, DATE_TIME_FORMATTER);
            } catch (DateTimeParseException e) {
                try {
                    checkInTime = LocalDateTime.parse(attendanceDate + " " + checkInStr, DATE_TIME_FORMATTER);
                } catch (DateTimeParseException ex) {
                    checkInTime = null;
                }
            }
        }

        if (status.isEmpty()) {
            status = "NORMAL";
        } else if (!VALID_STATUSES.contains(status)) {
            return "第 " + rowNum + " 行：状态值不正确（可选值：NORMAL/LATE/EARLY/ABSENT）";
        }

        Attendance attendance = new Attendance();
        attendance.setStudentNo(studentNo);
        attendance.setStudentName(student.getName());
        attendance.setClassName(student.getClassName());
        attendance.setCourseId(courseId);
        attendance.setAttendanceDate(attendanceDate);
        attendance.setCheckInTime(checkInTime);
        attendance.setStatus(status);
        attendance.setEarlyLeave("EARLY".equals(status));
        attendance.setRemark(remark);
        attendance.setCreateTime(LocalDateTime.now());
        attendance.setUpdateTime(LocalDateTime.now());

        attendanceRepository.save(attendance);
        return null;
    }

    /**
     * 获取某学生的统计页面数据（按周 + 按月）
     *
     * 【功能】
     * 1. 计算整个日期范围的汇总数据（总次数、出勤、缺勤、出勤率）
     * 2. 按周拆分：每周一段，从 startDate 开始每7天一段
     * 3. 按月拆分：每月一段，自动处理跨年情况
     *
     * 【空值安全】
     * 如果 studentNo / startDate / endDate 为空，直接返回空对象。
     * 不会报空指针异常。
     *
     * @param studentNo 学号
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计页面数据对象（含汇总、周统计、月统计）
     */
    public AttendanceStatisticsPageDto getAttendanceStatistics(String studentNo, LocalDate startDate, LocalDate endDate) {
        AttendanceStatisticsPageDto pageDto = new AttendanceStatisticsPageDto();

        // 参数校验：任何一个为空就不查询
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

        // ---- 数据库已有统计（保留） ----
        long dbPresentCnt = attendanceRepository.countByStudentNoAndAttendanceDateBetweenAndStatusIn(
                studentNo, startDate, endDate, PRESENT_STATUSES
        );
        long dbAbsentCnt = attendanceRepository.countByStudentNoAndAttendanceDateBetweenAndStatus(
                studentNo, startDate, endDate, ABSENT_STATUS
        );

        // ---- 遍历每门课×每天：有记录=出勤，无记录=缺勤 ----
        long rangeTotal = 0;
        long rangePresent = 0;
        Student student = studentRepository.findByStudentNo(studentNo);
        if (student != null && student.getClassName() != null) {
            List<Course> classCourses = courseRepository.findByClassName(student.getClassName());
            if (!classCourses.isEmpty()) {
                LocalDate current = startDate;
                while (!current.isAfter(endDate)) {
                    if (current.getDayOfWeek() != DayOfWeek.SATURDAY && current.getDayOfWeek() != DayOfWeek.SUNDAY) {
                        for (Course course : classCourses) {
                            rangeTotal++;
                            Optional<Attendance> record = attendanceRepository
                                    .findByStudentNoAndCourseIdAndAttendanceDate(studentNo, course.getId(), current);
                            if (record.isPresent()) {
                                rangePresent++;
                            }
                        }
                    }
                    current = current.plusDays(1);
                }
            }
        }
        if (rangeTotal == 0) {
            rangeTotal = dbPresentCnt + dbAbsentCnt;
            rangePresent = dbPresentCnt;
        }

        pageDto.setRangeTotalCount(rangeTotal);
        pageDto.setRangePresentCount(rangePresent);
        pageDto.setRangeAbsentCount(rangeTotal - rangePresent);
        pageDto.setRangeAttendanceRate(calculateRate(rangePresent, rangeTotal));

        pageDto.setWeeklyStatistics(buildWeeklyStatistics(studentNo, startDate, endDate));
        pageDto.setMonthlyStatistics(buildMonthlyStatistics(studentNo, startDate, endDate));

        return pageDto;
    }

    /**
     * 按周拆分统计
     *
     * 【算法】
     * 从 startDate 开始，每7天分为一周，直到超过 endDate。
     * 最后一周可能不足7天（截断到 endDate）。
     *
     * 例如：2025-05-01 ~ 2025-05-20
     *   第1周：05-01 ~ 05-07
     *   第2周：05-08 ~ 05-14
     *   第3周：05-15 ~ 05-20（不足7天）
     *
     * @param studentNo 学号
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 每周一条统计记录的列表
     */
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

    /**
     * 构建单个统计区间的数据（按周或按月）
     *
     * 【改进后的统计逻辑】
     * 之前：只统计 attendance 表中已有的记录 → 没打卡的"失踪"学生不被计入
     * 现在：基于课程表计算"应有出勤次数" → 有课但没打卡 = 缺勤
     *
     * 计算步骤：
     * 1. 查出学生所在的班级（className）
     * 2. 查出该班级的所有课程
     * 3. 遍历区间内的每个工作日（周一到周五）：
     *    - 对于每门课程，查询是否有考勤记录
     *    - 有记录 → 按实际状态统计（NORMAL/LATE/EARLY 算出席）
     *    - 无记录 → 按缺勤计
     */
    private AttendanceStatisticsItemDto buildStatisticsItem(String studentNo, LocalDate startDate, LocalDate endDate, String label) {
        // ---- 数据库已有统计（保留查询，后续可复用） ----
        long dbPresentCount = attendanceRepository.countByStudentNoAndAttendanceDateBetweenAndStatusIn(
                studentNo, startDate, endDate, PRESENT_STATUSES
        );
        long dbAbsentCount = attendanceRepository.countByStudentNoAndAttendanceDateBetweenAndStatus(
                studentNo, startDate, endDate, ABSENT_STATUS
        );

        // ---- 基于课程表：有记录=出勤，无记录=缺勤 ----
        Student student = studentRepository.findByStudentNo(studentNo);
        long total = 0;
        long present = 0;

        if (student != null && student.getClassName() != null) {
            List<Course> classCourses = courseRepository.findByClassName(student.getClassName());
            if (!classCourses.isEmpty()) {
                LocalDate current = startDate;
                while (!current.isAfter(endDate)) {
                    if (current.getDayOfWeek() != DayOfWeek.SATURDAY && current.getDayOfWeek() != DayOfWeek.SUNDAY) {
                        for (Course course : classCourses) {
                            total++;
                            Optional<Attendance> record = attendanceRepository
                                    .findByStudentNoAndCourseIdAndAttendanceDate(studentNo, course.getId(), current);
                            if (record.isPresent()) {
                                present++;
                            }
                        }
                    }
                    current = current.plusDays(1);
                }
            }
        }
        if (total == 0) {
            total = dbPresentCount + dbAbsentCount;
            present = dbPresentCount;
        }

        AttendanceStatisticsItemDto item = new AttendanceStatisticsItemDto();
        item.setLabel(label);
        item.setTotalCount(total);
        item.setPresentCount(present);
        item.setAbsentCount(total - present);
        item.setAttendanceRate(calculateRate(present, total));

        return item;
    }

    private double calculateRate(long presentCount, long totalCount) {
        if (totalCount == 0) {
            return 0.0;
        }
        return Math.round(presentCount * 10000.0 / totalCount) / 100.0;
    }

    /**
     * 获取某门课程的考勤统计（教师用）
     *
     * @param courseId  课程 ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param totalStudents 该课程班级的总人数
     * @return 统计结果 Map，含 expectedTotal、presentCount、absentCount、attendanceRate
     */
    public Map<String, Object> getCourseAttendanceStats(Long courseId, LocalDate startDate, LocalDate endDate, int totalStudents) {
        Map<String, Object> result = new HashMap<>();

        long dbPresentCount = attendanceRepository.countByCourseIdAndAttendanceDateBetweenAndStatusIn(
                courseId, startDate, endDate, PRESENT_STATUSES);
        long dbAbsentCount = attendanceRepository.countByCourseIdAndAttendanceDateBetweenAndStatus(
                courseId, startDate, endDate, ABSENT_STATUS);

        // 计算期望总次数 = 总人数 × 工作日天数
        int weekdays = 0;
        LocalDate cur = startDate;
        while (!cur.isAfter(endDate)) {
            if (cur.getDayOfWeek() != DayOfWeek.SATURDAY && cur.getDayOfWeek() != DayOfWeek.SUNDAY) {
                weekdays++;
            }
            cur = cur.plusDays(1);
        }
        long expectedTotal = (long) totalStudents * weekdays;

        // 如果期望次数小于实际记录数，用实际记录数
        long total = Math.max(expectedTotal, dbPresentCount + dbAbsentCount);
        long absent = total - dbPresentCount;

        result.put("expectedTotal", total);
        result.put("presentCount", dbPresentCount);
        result.put("absentCount", absent);
        result.put("attendanceRate", calculateRate(dbPresentCount, total));
        result.put("totalStudents", totalStudents);
        result.put("weekdays", weekdays);

        return result;
    }

    // ========== 班级级统计 ==========

    public List<Map<String, Object>> getClassStatistics(LocalDate startDate, LocalDate endDate) {
        List<String> classNames = attendanceRepository.findDistinctClassNameBy();
        List<Map<String, Object>> result = new ArrayList<>();

        for (String className : classNames) {
            if (className == null || className.isEmpty()) continue;

            long totalCount = attendanceRepository.countByClassNameAndAttendanceDateBetween(className, startDate, endDate);
            if (totalCount == 0) continue;

            long presentCount = attendanceRepository.countByClassNameAndAttendanceDateBetweenAndStatusIn(
                    className, startDate, endDate, PRESENT_STATUSES);
            long absentCount = attendanceRepository.countByClassNameAndAttendanceDateBetweenAndStatus(
                    className, startDate, endDate, ABSENT_STATUS);

            Map<String, Object> item = new HashMap<>();
            item.put("className", className);
            item.put("totalCount", totalCount);
            item.put("presentCount", presentCount);
            item.put("absentCount", absentCount);
            item.put("attendanceRate", calculateRate(presentCount, totalCount));
            result.add(item);
        }

        return result;
    }
}