package com.example.attendance.controller;

/**
 * 学生管理控制器 —— 学生信息的增删改查 + Excel 导入
 *
 * 【@Controller 注解】
 * 告诉 Spring 这是一个控制器类，Spring 会扫描它，
 * 将 @GetMapping/@PostMapping 注册到路由表。
 * 方法返回 String = Thymeleaf 模板的文件名（不含 .html 后缀）。
 *
 * 【@RequestMapping("/student")】
 * 给整个控制器加一个"前缀路径"。
 * 这样 @GetMapping("/list") 的实际路由就是 /student/list，
 * 避免了每个方法都写 /student/list、/student/add...
 */

import com.example.attendance.dto.StudentImportResult;
import com.example.attendance.entity.Student;
import com.example.attendance.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private StudentService studentService;

    /**
     * 学生列表页（GET 请求）
     *
     * 【功能】从数据库查出所有学生，展示在表格中。
     * 页面上有「新增学生」和「导入学生」按钮。
     *
     * studentService.findAll() 调用 JPA 的 findAll() 方法，
     * 自动生成 SELECT * FROM student 语句。
     *
     * @param model 向模板传数据，模板中用 ${students} 遍历
     * @return 学生列表模板
     */
    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("students", studentService.findAll());
        return "student-list";
    }

    /**
     * 新增学生页面（GET 请求）
     *
     * 【功能】展示一个空白的学生表单，让用户填写学生信息。
     * 表单提交到 POST /student/save。
     *
     * model.addAttribute("student", new Student()) 创建一个空学生对象，
     * Thymeleaf 用 th:object="${student}" 绑定这个对象，
     * 表单输入框用 th:field="*{name}" 绑定到对象的属性。
     *
     * @param model 向模板传数据
     * @return 学生表单模板（标题显示"新增学生"）
     */
    @GetMapping("/add")
    public String addPage(Model model) {
        model.addAttribute("student", new Student());
        model.addAttribute("title", "新增学生");
        return "student-form";
    }

    /**
     * 保存新增学生（POST 请求）
     *
     * 【@Valid 注解】
     * 自动触发 Student 实体上的 JSR-303 校验注解：
     *   @NotBlank(message = "学号不能为空")  → 学号不能为空
     * 如果校验不通过，BindingResult 中会包含错误信息。
     *
     * 【BindingResult】
     * 必须紧跟在 @Valid 参数后面，否则 Spring 在校验失败时会直接抛出异常。
     * BindingResult.hasErrors() = 是否有校验错误。
     *
     * 自定义校验：检查学号是否已存在（数据库唯一约束）。
     *
     * @param student       前端提交的学生信息（自动封装）
     * @param bindingResult 校验结果
     * @param model         向模板传数据
     * @return 成功→重定向到列表页，失败→返回表单页并显示错误
     */
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("student") Student student,
                       BindingResult bindingResult,
                       Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("title", "新增学生");
            return "student-form";
        }

        if (studentService.existsByStudentNo(student.getStudentNo())) {
            model.addAttribute("title", "新增学生");
            model.addAttribute("studentNoError", "学号已存在");
            return "student-form";
        }

        studentService.save(student);
        return "redirect:/student/list";
    }

    /**
     * 编辑学生页面（GET 请求）
     *
     * 【@PathVariable】
     * 从 URL 路径中获取参数。比如 /student/edit/3，id = 3。
     * URL 模式 /edit/{id} 中的 {id} 就是路径变量。
     *
     * 【业务流程】
     * 1. 根据 id 从数据库查出学生
     * 2. 如果没找到，重定向到列表页
     * 3. 如果找到了，把学生信息传给表单模板
     * 4. 表单自动回填该学生的所有字段
     *
     * @param id    要编辑的学生 ID
     * @param model 向模板传数据
     * @return 学生表单模板（标题显示"编辑学生"）
     */
    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable Long id, Model model) {
        Student student = studentService.findById(id);
        if (student == null) {
            return "redirect:/student/list";
        }
        model.addAttribute("student", student);
        model.addAttribute("title", "编辑学生");
        return "student-form";
    }

    /**
     * 更新学生信息（POST 请求）
     *
     * 【更新 vs 新增的区别】
     * 编辑时 student 对象已经有一个 id（隐藏字段传递），
     * 新增时 student.id = null。
     *
     * 【学号唯一性检查】
     * 编辑时也可能修改学号，所以要检查新学号是否已被其他学生占用。
     * findByStudentNo() 查到的学生如果不是当前编辑的学生，说明学号重复。
     *
     * @param student       前端提交的学生信息（含 id）
     * @param bindingResult 校验结果
     * @param model         向模板传数据
     * @return 成功→重定向到列表页，失败→返回表单页
     */
    @PostMapping("/update")
    public String update(@Valid @ModelAttribute("student") Student student,
                         BindingResult bindingResult,
                         Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("title", "编辑学生");
            return "student-form";
        }

        Student oldStudent = studentService.findByStudentNo(student.getStudentNo());
        if (oldStudent != null && !oldStudent.getId().equals(student.getId())) {
            model.addAttribute("title", "编辑学生");
            model.addAttribute("studentNoError", "学号已存在");
            return "student-form";
        }

        studentService.update(student);
        return "redirect:/student/list";
    }

    /**
     * 删除学生（POST 请求）
     *
     * 【为什么用 POST 而不是 GET？】
     * 删除操作会修改数据，应该用 POST（或 DELETE）而不是 GET，
     * 防止搜索引擎爬虫或误点链接导致意外删除。
     *
     * 【@PathVariable Long id】
     * 从 URL 路径中获取要删除的学生 ID。
     * URL：/student/delete/3  → 删除 id=3 的学生
     *
     * 【删除前检查】
     * 虽然 StudentService.deleteById() 中已经做了空值判断，
     * 但这里还是加上 findById 检查，方便给出友好提示。
     *
     * @param id 要删除的学生 ID
     * @return 重定向到学生列表页
     */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        // 先检查学生是否存在，不存在也重定向（不做特殊处理）
        studentService.deleteById(id);
        return "redirect:/student/list";
    }

    /**
     * 学生导入页面（GET 请求）
     *
     * 【功能】展示上传 Excel 的页面，用于批量导入学生。
     * Excel 格式：学号 | 姓名 | 性别 | 学院 | 年级 | 专业 | 班级
     */
    @GetMapping("/import")
    public String importPage() {
        return "student-import";
    }

    /**
     * 上传 Excel 并批量导入学生（POST 请求）
     *
     * 【业务流程】
     * 1. 检查文件是否为空
     * 2. 检查扩展名是否为 .xlsx
     * 3. 调用 studentService.importStudents() 处理导入
     * 4. 把导入结果传给模板展示
     *
     * 具体的Excel解析和校验逻辑在 StudentService.importStudents() 中。
     *
     * @param file  上传的 Excel 文件
     * @param model 向模板传数据
     * @return 导入页面（显示结果）
     */
    @PostMapping("/import")
    public String importStudents(@RequestParam("file") MultipartFile file, Model model) {
        if (file == null || file.isEmpty()) {
            model.addAttribute("error", "请选择要上传的 Excel 文件");
            return "student-import";
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".xlsx")) {
            model.addAttribute("error", "目前只支持 .xlsx 格式的 Excel 文件");
            return "student-import";
        }


        //  判断 contentType（辅助校验，不绝对依赖）
        String contentType = file.getContentType();
        if (contentType != null &&
                !contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
            // 这里只做辅助，不强制 return，避免某些浏览器传的 MIME 不标准
        }

        StudentImportResult result = studentService.importStudents(file);
        model.addAttribute("result", result);

        return "student-import";
    }
}