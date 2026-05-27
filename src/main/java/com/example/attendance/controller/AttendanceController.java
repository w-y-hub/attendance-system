package com.example.attendance.controller;

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

    @GetMapping("/attendance/checkIn")
    public String checkInPage(Model model,
                              @RequestParam(required = false) String success,
                              @RequestParam(required = false) String error) {
        model.addAttribute("courses", courseService.findAll());
        model.addAttribute("success", success);
        model.addAttribute("error", error);
        return "attendance-check-in";
    }

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

    @GetMapping("/attendance/import")
    public String importPage() {
        return "attendance-import";
    }

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

    private String encode(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }
}