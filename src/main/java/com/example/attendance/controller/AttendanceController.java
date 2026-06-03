package com.example.attendance.controller;

/**
 * 考勤管理控制器 —— 处理打卡、列表查询、统计、导入导出等
 *
 * 这是一个"大控制器"，包含了考勤相关的所有功能：
 *   签到/签退 → POST /attendance/checkIn 和 /attendance/checkOut
 *   列表查询 → GET /attendance/list（支持多条件筛选+分页）
 *   统计    → GET /attendance/statistics（个人）和 /attendance/statistics/class（班级）
 *   导入    → POST /attendance/import（Excel 批量导入）
 *   导出    → GET /attendance/export（CSV 下载）
 *
 * 【构造方法注入】
 * AttendanceService、StudentService、CourseService 通过构造方法传入，
 * 这是 Spring 官方推荐的注入方式（见 CourseService 中的说明）。
 */

import com.example.attendance.dto.AttendanceStatisticsPageDto;
import com.example.attendance.dto.ImportResult;
import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.Course;
import com.example.attendance.entity.Student;
import com.example.attendance.service.AttendanceService;
import com.example.attendance.service.CourseService;
import com.example.attendance.service.StudentService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Controller
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final StudentService studentService;
    private final CourseService courseService;

    public AttendanceController(AttendanceService attendanceService,
                                StudentService studentService,
                                CourseService courseService) {
        this.attendanceService = attendanceService;
        this.studentService = studentService;
        this.courseService = courseService;
    }

    /**
     * 显示签到页面（GET 请求）
     *
     * 【功能】展示考勤打卡页面，包含课程下拉选择框。
     * 也可以显示签到成功/失败的提示信息（通过 URL 参数传递）。
     *
     * @param model   Spring 的 Model 对象，用于向模板传数据
     * @param success URL 参数，签到成功时传 "签到成功"
     * @param error   URL 参数，签到失败时传错误描述
     * @return 模板名称 attendance-check-in
     *
     * 【@RequestParam(required = false)】
     * 表示这个参数不是必须的——第一次进入页面时没有 success/error 参数，
     * 签到成功后重定向回来时才会带上这些参数。
     */
    @GetMapping("/attendance/checkIn")
    public String checkInPage(Model model,
                              @RequestParam(required = false) String success,
                              @RequestParam(required = false) String error) {
        model.addAttribute("courses", courseService.findAll());
        model.addAttribute("success", success);
        model.addAttribute("error", error);
        return "attendance-check-in";
    }

    /**
     * 处理签到请求（POST 请求）
     *
     * 【业务流程】
     * 1. 验证当前用户是否已登录（Principal 不能为空）
     * 2. 从数据库查出学生信息和课程信息
     * 3. 检查当前时间是否在允许签到的时间段内（课前15分钟 ~ 课后30分钟）
     * 4. 检查今天该课程是否已签到（防止重复打卡）
     * 5. 创建一条新的考勤记录并保存
     * 6. 如果签到时间晚于课程开始时间，状态设为"迟到"（LATE），否则"正常"（NORMAL）
     *
     * 【redirect 重定向】
     * 签到成功后 redirect 回签到页面，并带上 success 参数显示成功提示。
     * 重定向会刷新页面，防止用户按 F5 时重复提交签到请求。
     *
     * @param courseId 签到对应的课程 ID
     * @param remark   备注信息（可选，如"身体不适"）
     * @param principal Spring Security 自动注入的当前登录用户
     * @return 重定向到签到页面（带 success/error 参数）
     */
    @PostMapping("/attendance/checkIn")
    public String checkIn(@RequestParam Long courseId,
                          @RequestParam(required = false) String remark,
                          Principal principal) {

        if (principal == null) {
            return "redirect:/login";
        }

        String studentNo = principal.getName();
        Student student = studentService.findByStudentNo(studentNo);
        if (student == null) {
            return "redirect:/attendance/checkIn?error=" + encode("当前学生不存在，打卡失败");
        }

        Course course = courseService.findById(courseId);
        if (course == null) {
            return "redirect:/attendance/checkIn?error=" + encode("课程不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalTime nowTime = now.toLocalTime();

        LocalTime startTime = course.getStartTime();
        LocalTime endTime = course.getEndTime();

        if (startTime == null || endTime == null) {
            return "redirect:/attendance/checkIn?error=" + encode("课程时间未配置完整");
        }

        LocalTime allowedStart = startTime.minusMinutes(15);
        LocalTime allowedEnd = startTime.plusMinutes(30);

        if (nowTime.isBefore(allowedStart) || nowTime.isAfter(allowedEnd)) {
            return "redirect:/attendance/checkIn?error=" + encode("当前不在允许签到时间内（课程开始前15分钟到开始后30分钟）");
        }

        Optional<Attendance> existing = attendanceService.findByStudentNoAndCourseIdAndAttendanceDate(studentNo, courseId, today);
        if (existing.isPresent()) {
            return "redirect:/attendance/checkIn?error=" + encode("今天该课程已签到，请勿重复打卡");
        }

        Attendance attendance = new Attendance();
        attendance.setStudentNo(student.getStudentNo());
        attendance.setStudentName(student.getName());
        attendance.setClassName(student.getClassName());
        attendance.setCourseId(courseId);
        attendance.setAttendanceDate(today);
        attendance.setRemark(remark);
        attendance.setCreateTime(now);
        attendance.setUpdateTime(now);
        attendance.setCheckInTime(now);

        if (nowTime.isAfter(startTime)) {
            attendance.setStatus("LATE");
        } else {
            attendance.setStatus("NORMAL");
        }

        attendance.setEarlyLeave(false);

        attendanceService.save(attendance);

        return "redirect:/attendance/checkIn?success=" + encode("签到成功");
    }

    /**
     * 处理签退请求（POST 请求）
     *
     * 【业务流程】
     * 1. 查出今天的签到记录（必须先签到才能签退）
     * 2. 检查是否已签退（防止重复操作）
     * 3. 记录签退时间，如果早于课程结束时间则标记为"早退"
     *
     * 【状态变化】
     * 正常签退：status 保持 NORMAL，earlyLeave = false
     * 早退签退：status 变为 EARLY，earlyLeave = true
     *
     * @param courseId 要签退的课程 ID
     * @param principal 当前登录用户
     * @return 重定向到签到页面
     */
    @PostMapping("/attendance/checkOut")
    public String checkOut(@RequestParam Long courseId,
                           Principal principal) {

        if (principal == null) {
            return "redirect:/login";
        }

        String studentNo = principal.getName();
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        LocalTime nowTime = now.toLocalTime();

        Course course = courseService.findById(courseId);
        if (course == null) {
            return "redirect:/attendance/checkIn?error=" + encode("课程不存在");
        }

        Optional<Attendance> optional = attendanceService.findByStudentNoAndCourseIdAndAttendanceDate(studentNo, courseId, today);
        if (optional.isEmpty()) {
            return "redirect:/attendance/checkIn?error=" + encode("请先签到，再签退");
        }

        Attendance attendance = optional.get();

        if (attendance.getCheckOutTime() != null) {
            return "redirect:/attendance/checkIn?error=" + encode("今天该课程已签退，请勿重复操作");
        }

        attendance.setCheckOutTime(now);

        LocalTime endTime = course.getEndTime();
        if (endTime != null && nowTime.isBefore(endTime)) {
            attendance.setEarlyLeave(true);
            attendance.setStatus("EARLY");
        }

        attendance.setUpdateTime(now);
        attendanceService.save(attendance);

        return "redirect:/attendance/checkIn?success=" + encode("签退成功");
    }

    /**
     * 考勤记录列表页（GET 请求）
     *
     * 【功能】展示当前学生的考勤记录，支持多条件筛选和分页。
     *
     * 【筛选条件】
     * - startDate / endDate：按日期范围筛选
     * - status：按状态筛选（NORMAL / LATE / EARLY / ABSENT）
     * - courseId：按课程筛选
     * - quickRange：快速筛选（today / week / month），会覆盖 startDate/endDate
     *
     * 【分页】
     * page = 当前页码（从1开始），size = 每页条数（默认10条）
     * Spring Data JPA 的 Pageable 从0开始计数，所以传参时要 page - 1
     *
     * 【Specification 动态查询】
     * 这是 JPA 的一种动态查询方式，可以在代码里拼装 WHERE 条件：
     *   spec.and(cb.equal(root.get("studentNo"), studentNo))   → WHERE student_no = ?
     *   spec.and(cb.greaterThanOrEqualTo(root.get("date"), d)) → AND date >= ?
     * 条件非必填，用户没选的筛选项不会加到 SQL 里。
     *
     * @param principal 当前登录用户
     * @param model     向模板传数据
     * @return 考勤记录列表页
     */
    @GetMapping("/attendance/list")
    public String list(@RequestParam(required = false) String startDate,
                       @RequestParam(required = false) String endDate,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) Long courseId,
                       @RequestParam(required = false) String quickRange,
                       @RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(required = false) String success,
                       Principal principal,
                       Model model) {

        if (principal == null) {
            return "redirect:/login";
        }

        String studentNo = principal.getName();

        LocalDate start = null;
        LocalDate end = null;

        if (quickRange != null && !quickRange.isEmpty()) {
            LocalDate today = LocalDate.now();
            switch (quickRange) {
                case "today" -> {
                    start = today;
                    end = today;
                }
                case "week" -> {
                    start = today.with(DayOfWeek.MONDAY);
                    end = today.with(DayOfWeek.SUNDAY);
                }
                case "month" -> {
                    start = today.withDayOfMonth(1);
                    end = today.withDayOfMonth(today.lengthOfMonth());
                }
            }
        } else {
            if (startDate != null && !startDate.isEmpty()) {
                start = LocalDate.parse(startDate);
            }
            if (endDate != null && !endDate.isEmpty()) {
                end = LocalDate.parse(endDate);
            }
        }

        Specification<Attendance> spec = (root, query, cb) ->
                cb.equal(root.get("studentNo"), studentNo);

        if (start != null) {
            LocalDate finalStart = start;
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("attendanceDate"), finalStart));
        }

        if (end != null) {
            LocalDate finalEnd = end;
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("attendanceDate"), finalEnd));
        }

        if (status != null && !status.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), status));
        }

        if (courseId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("courseId"), courseId));
        }

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Attendance> recordPage = attendanceService.findAll(spec, pageable);

        model.addAttribute("records", recordPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", recordPage.getTotalPages());

        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("status", status);
        model.addAttribute("courseId", courseId);
        model.addAttribute("quickRange", quickRange);
        List<Course> allCourses = courseService.findAll();
        Map<Long, String> courseMap = new HashMap<>();
        for (Course c : allCourses) {
            courseMap.put(c.getId(), c.getCourseName());
        }
        model.addAttribute("courseMap", courseMap);
        model.addAttribute("courses", allCourses);
        model.addAttribute("success", success);

        return "attendance-list";
    }

    /**
     * 导出考勤记录为 CSV 文件（GET 请求）
     *
     * 【什么是 CSV？】
     * CSV（Comma-Separated Values）是一种用逗号分隔数据的文本格式。
     * 可以用 Excel 打开，适合做数据分析和留存。
     *
     * 【方法返回值是 void】
     * 这个方法不返回视图名称，而是直接往 HTTP 响应里写入 CSV 数据。
     * 通过 HttpServletResponse 对象设置响应头（Content-Disposition），
     * 浏览器收到后会弹出"下载文件"对话框。
     *
     * 【工作原理】
     * 1. 根据筛选条件查询考勤记录
     * 2. 设置响应头：告诉浏览器这是一个 CSV 附件
     * 3. 用 PrintWriter 逐行写入 CSV 数据
     * 4. flush() 把数据推送给浏览器
     *
     * @param principal 当前登录用户
     * @param response  HTTP 响应对象，用于直接写文件内容
     */
    @GetMapping("/attendance/export")
    public void export(@RequestParam(required = false) String startDate,
                       @RequestParam(required = false) String endDate,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) Long courseId,
                       @RequestParam(required = false) String quickRange,
                       Principal principal,
                       HttpServletResponse response) throws Exception {

        if (principal == null) {
            response.sendRedirect("/login");
            return;
        }

        String studentNo = principal.getName();

        LocalDate start = null;
        LocalDate end = null;

        if (quickRange != null && !quickRange.isEmpty()) {
            LocalDate today = LocalDate.now();
            switch (quickRange) {
                case "today" -> {
                    start = today;
                    end = today;
                }
                case "week" -> {
                    start = today.with(DayOfWeek.MONDAY);
                    end = today.with(DayOfWeek.SUNDAY);
                }
                case "month" -> {
                    start = today.withDayOfMonth(1);
                    end = today.withDayOfMonth(today.lengthOfMonth());
                }
            }
        } else {
            if (startDate != null && !startDate.isEmpty()) {
                start = LocalDate.parse(startDate);
            }
            if (endDate != null && !endDate.isEmpty()) {
                end = LocalDate.parse(endDate);
            }
        }

        Specification<Attendance> spec = (root, query, cb) ->
                cb.equal(root.get("studentNo"), studentNo);

        if (start != null) {
            LocalDate finalStart = start;
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("attendanceDate"), finalStart));
        }

        if (end != null) {
            LocalDate finalEnd = end;
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("attendanceDate"), finalEnd));
        }

        if (status != null && !status.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), status));
        }

        if (courseId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("courseId"), courseId));
        }

        List<Attendance> list = attendanceService.findAll(spec);

        Map<Long, String> courseMap = new HashMap<>();
        for (Course c : courseService.findAll()) {
            courseMap.put(c.getId(), c.getCourseName());
        }

        response.setContentType("text/csv;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=" + URLEncoder.encode("attendance.csv", StandardCharsets.UTF_8));

        PrintWriter writer = response.getWriter();
        writer.println("日期,课程名称,签到时间,签退时间,状态,是否早退,备注");

        for (Attendance a : list) {
            String cName = a.getCourseId() == null ? "" : courseMap.getOrDefault(a.getCourseId(), String.valueOf(a.getCourseId()));
            writer.printf("%s,%s,%s,%s,%s,%s,%s%n",
                    a.getAttendanceDate() == null ? "" : a.getAttendanceDate(),
                    cName,
                    a.getCheckInTime() == null ? "" : a.getCheckInTime(),
                    a.getCheckOutTime() == null ? "" : a.getCheckOutTime(),
                    a.getStatus() == null ? "" : a.getStatus(),
                    a.getEarlyLeave() == null ? "" : (a.getEarlyLeave() ? "是" : "否"),
                    a.getRemark() == null ? "" : a.getRemark().replace(",", "，"));
        }

        writer.flush();
    }

    /**
     * 显示考勤导入页面（GET 请求）
     *
     * 【功能】展示上传 Excel 文件的页面。
     * 该页面包含文件选择框、上传按钮、以及 Excel 格式说明。
     *
     * 注意：导入考勤记录需要上传格式正确的 Excel，
     * 列顺序：学号 | 课程名称 | 考勤日期 | 签到时间 | 状态 | 备注
     * 如果要导入学生信息，请使用 /student/import
     */
    @GetMapping("/attendance/import")
    public String importPage() {
        return "attendance-import";
    }

    /**
     * 上传 Excel 并批量导入考勤记录（POST 请求）
     *
     * 【业务流程】
     * 1. 验证上传的文件是否为空
     * 2. 验证文件扩展名是否为 .xlsx 或 .xls
     * 3. 调用 attendanceService.importFromExcel() 进行实际导入
     * 4. 把导入结果（成功/失败条数、错误详情）通过 flash 属性传给页面
     *
     * 【RedirectAttributes】
     * 是 Model 的一种变体，专门用于重定向后传递数据。
     * addFlashAttribute() 的数据只会存活一次重定向（刷新页面就没了），
     * 适用于显示一次性提示信息。
     *
     * @param file               上传的 Excel 文件
     * @param redirectAttributes 用于重定向后传递结果数据
     * @return 重定向到考勤导入页面（显示导入结果）
     */
    @PostMapping("/attendance/import")
    public String importAttendance(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {
        if (file == null || file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "请选择要上传的 Excel 文件");
            return "redirect:/attendance/import";
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !(fileName.toLowerCase().endsWith(".xlsx") || fileName.toLowerCase().endsWith(".xls"))) {
            redirectAttributes.addFlashAttribute("error", "请上传 .xlsx 或 .xls 格式的 Excel 文件");
            return "redirect:/attendance/import";
        }

        // 检查文件实际大小
        if (file.getSize() > 10 * 1024 * 1024) {
            redirectAttributes.addFlashAttribute("error", "文件大小超过 10MB 限制");
            return "redirect:/attendance/import";
        }

        try {
            ImportResult result = attendanceService.importFromExcel(file);
            redirectAttributes.addFlashAttribute("result", result);
            redirectAttributes.addFlashAttribute("success",
                    "导入完成：总共 " + result.getTotalCount() + " 条，成功 " + result.getSuccessCount() + " 条，失败 " + result.getFailCount() + " 条");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "导入失败：" + e.getMessage());
        }

        return "redirect:/attendance/import";
    }

    /**
     * 个人考勤统计页面（GET 请求）
     *
     * 【功能】展示当前学生的考勤统计，支持按日期范围查询。
     *
     * 【统计内容】
     * - 整个日期范围的汇总（总次数、出勤次数、缺勤次数、出勤率）
     * - 按周细分统计（每周一条记录）
     * - 按月细分统计（每月一条记录）
     * - 出勤率图表（Chart.js 柱状图）
     *
     * 【出勤率计算】
     * 出勤率 = (NORMAL + LATE + EARLY 的次数) / 总考勤次数 × 100%
     * 注意：LATE（迟到）和 EARLY（早退）也算"出勤"，只有 ABSENT 算缺勤。
     *
     * @param startDateStr 开始日期（格式：yyyy-MM-dd）
     * @param endDateStr   结束日期（格式：yyyy-MM-dd）
     * @param principal    当前登录用户
     * @param model        向模板传数据
     * @return 考勤统计页面
     */
    @GetMapping("/attendance/statistics")
    public String statisticsPage(@RequestParam(value = "startDate", required = false) String startDateStr,
                                 @RequestParam(value = "endDate", required = false) String endDateStr,
                                 Principal principal,
                                 Model model) {

        if (principal == null) {
            return "redirect:/login";
        }

        String studentNo = principal.getName();
        AttendanceStatisticsPageDto statistics = new AttendanceStatisticsPageDto();

        model.addAttribute("startDate", startDateStr);
        model.addAttribute("endDate", endDateStr);

        if (startDateStr == null || startDateStr.trim().isEmpty()
                || endDateStr == null || endDateStr.trim().isEmpty()) {
            model.addAttribute("statistics", statistics);
            return "attendance-statistics";
        }

        try {
            LocalDate startDate = LocalDate.parse(startDateStr);
            LocalDate endDate = LocalDate.parse(endDateStr);

            if (startDate.isAfter(endDate)) {
                model.addAttribute("errorMessage", "开始日期不能晚于结束日期");
                model.addAttribute("statistics", statistics);
                return "attendance-statistics";
            }

            statistics = attendanceService.getAttendanceStatistics(studentNo, startDate, endDate);
            model.addAttribute("statistics", statistics);

        } catch (Exception e) {
            model.addAttribute("errorMessage", "日期格式不正确，请按 yyyy-MM-dd 格式输入");
            model.addAttribute("statistics", statistics);
        }

        return "attendance-statistics";
    }

    /**
     * 班级考勤统计页面（GET 请求）
     *
     * 【功能】展示各个班级的出勤率对比。
     * 管理员和教师可以用这个页面查看哪些班级出勤率高、哪些需要关注。
     *
     * 【与个人统计的区别】
     * - 个人统计（/attendance/statistics）：看"我"的出勤情况
     * - 班级统计（/attendance/statistics/class）：看"每个班"的出勤率
     *
     * 统计逻辑在 AttendanceService.getClassStatistics() 中，
     * 它会遍历所有班级，计算每个班在指定日期范围内的出勤率。
     *
     * @param startDateStr 开始日期
     * @param endDateStr   结束日期
     * @param model        向模板传数据
     * @return 班级统计页面
     */
    @GetMapping("/attendance/statistics/class")
    public String classStatisticsPage(@RequestParam(value = "startDate", required = false) String startDateStr,
                                      @RequestParam(value = "endDate", required = false) String endDateStr,
                                      Model model) {
        model.addAttribute("startDate", startDateStr);
        model.addAttribute("endDate", endDateStr);

        if (startDateStr != null && !startDateStr.trim().isEmpty()
                && endDateStr != null && !endDateStr.trim().isEmpty()) {
            try {
                LocalDate startDate = LocalDate.parse(startDateStr);
                LocalDate endDate = LocalDate.parse(endDateStr);
                if (!startDate.isAfter(endDate)) {
                    model.addAttribute("classStats", attendanceService.getClassStatistics(startDate, endDate));
                } else {
                    model.addAttribute("errorMessage", "开始日期不能晚于结束日期");
                }
            } catch (Exception e) {
                model.addAttribute("errorMessage", "日期格式不正确");
            }
        }
        return "attendance-statistics-class";
    }

    /**
     * 对字符串进行 URL 编码
     *
     * 【为什么需要这个？】
     * 当我们在重定向 URL 中传递中文参数时，
     * 比如 redirect:/checkIn?error=签到成功，
     * URL 中的中文可能会乱码或出错。
     * URLEncoder.encode() 把中文转成 %xx 格式，保证 URL 正确。
     *
     * 例如："签到成功" → URL 编码后 → "%E7%AD%BE%E5%88%B0%E6%88%90%E5%8A%9F"
     *
     * @param text 需要编码的文本（可能是中文）
     * @return URL 编码后的字符串
     */
    private String encode(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }
}