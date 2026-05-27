package com.example.attendance.service;

/**
 * 学生业务逻辑层 —— 负责处理学生相关的"业务规则"
 *
 * 【三层架构回顾】
 * Controller（控制层）→ Service（业务层）→ Repository（数据访问层）
 * └─ 接收请求、返回页面     └─ 写业务逻辑       └─ 操作数据库
 *
 * 为什么要有 Service 层？
 * 如果把业务逻辑写在 Controller 里，会导致 Controller 又大又乱，
 * 而且多个 Controller 可能需要复用同一段逻辑（比如导入学生），
 * 抽到 Service 层就能复用。
 */

import com.example.attendance.dto.StudentImportResult;
import com.example.attendance.entity.Student;
import com.example.attendance.repository.StudentRepository;
import com.example.attendance.util.ExcelUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Service 注解：告诉 Spring 这是一个"服务类"，
 * Spring 会自动创建它的实例（依赖注入），
 * 其他类可以通过 @Autowired 拿到它。
 */
@Service
public class StudentService {

    /**
     * @Autowired：自动注入 —— Spring 会自动创建一个 StudentRepository 的实现，
     * 并赋值给这个变量。你不用自己 new，Spring 帮你管理对象的生命周期。
     */
    @Autowired
    private StudentRepository studentRepository;

    // ===================== 增删改查（CRUD） =====================

    /** 新增或保存学生（id 为 null 时 insert，有 id 时 update） */
    public void save(Student student) {
        studentRepository.save(student);
    }

    /** 根据主键 id 查找学生，查不到返回 null */
    public Student findById(Long id) {
        return studentRepository.findById(id).orElse(null);
    }

    /** 查询所有学生 */
    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    /**
     * 更新学生信息
     *
     * 【为什么要先查出来再 set？】
     * 因为前端只提交了表单里的字段，而数据库里还有 createTime 等字段。
     * 如果直接 save(student)，createTime 会被覆盖成 null。
     * 正确做法：先查出旧数据，只修改前端提交的字段。
     */
    public void update(Student student) {
        Student oldStudent = studentRepository.findById(student.getId()).orElse(null);
        if (oldStudent != null) {
            // 逐个字段更新（新增了 college / grade / major）
            oldStudent.setStudentNo(student.getStudentNo());
            oldStudent.setName(student.getName());
            oldStudent.setGender(student.getGender());
            oldStudent.setCollege(student.getCollege());
            oldStudent.setGrade(student.getGrade());
            oldStudent.setMajor(student.getMajor());
            oldStudent.setClassName(student.getClassName());
            oldStudent.setStatus(student.getStatus());
            studentRepository.save(oldStudent);
        }
    }

    /** 根据 id 删除 */
    public void deleteById(Long id) {
        studentRepository.deleteById(id);
    }

    /** 根据学号删除 */
    public void deleteByStudentNo(String studentNo) {
        Student student = studentRepository.findByStudentNo(studentNo);
        if (student != null) {
            studentRepository.deleteById(student.getId());
        }
    }

    /** 根据班级查询 */
    public List<Student> getByClassName(String className) {
        return studentRepository.findByClassName(className);
    }

    /** 判断学号是否已存在 */
    public boolean existsByStudentNo(String studentNo) {
        return studentRepository.existsByStudentNo(studentNo);
    }

    /** 根据学号查找学生 */
    public Student findByStudentNo(String studentNo) {
        return studentRepository.findByStudentNo(studentNo);
    }

    // ===================== Excel 批量导入 =====================

    /**
     * 从 Excel 文件批量导入学生 —— 核心方法
     *
     * 【Excel 列顺序】（从第0列开始数）
     * 列0：学号     （必填）
     * 列1：姓名     （必填）
     * 列2：性别     （必填，只能是"男"或"女"）
     * 列3：学院     （如：计算机与人工智能学院）
     * 列4：年级     （如：2022）
     * 列5：专业     （如：计算机科学与技术）
     * 列6：班级     （必填，如：2022级计算机科学与技术）
     *
     * 【处理逻辑】
     * 1. 检查文件是否为空、格式是否正确
     * 2. 逐行读取（从第2行开始，第1行是表头）
     * 3. 对每行做合法性校验
     * 4. 校验通过则创建 Student 对象并存入数据库
     * 5. 返回导入结果（成功数、失败数、错误信息列表）
     */
    public StudentImportResult importStudents(MultipartFile file) {
        StudentImportResult result = new StudentImportResult();

        // 【Tip】Set 是"集合"，元素不能重复。用它来记录 Excel 里出现过的学号，
        // 方便检测"同一份 Excel 里学号是否重复"。
        Set<String> excelStudentNoSet = new HashSet<>();

        // ---------- 文件基础校验 ----------

        if (file == null || file.isEmpty()) {
            result.addErrorMessage("上传文件不能为空");
            return result;
        }

        String fileName = file.getOriginalFilename();
        // 注意：这里只允许 .xlsx（Excel 2007+ 格式），早期的 .xls 不支持
        if (fileName == null || !fileName.toLowerCase().endsWith(".xlsx")) {
            result.addErrorMessage("请上传 .xlsx 格式的 Excel 文件");
            return result;
        }

        // ---------- 解析 Excel ----------

        /**
         * try-with-resources 语法（Java 7+）：
         * 在 try 的括号里打开的资源，会在 try 结束时自动关闭（close），
         * 不用手写 finally { xxx.close() }。
         *
         * XSSFWorkbook = 解析 .xlsx 文件的类（Apache POI 库提供）
         */
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            // getSheetAt(0)：取第一个工作表
            Sheet sheet = workbook.getSheetAt(0);
            // getLastRowNum()：最后一行行号（从0开始计数），
            // 所以如果只有表头没有数据，lastRowNum = 0
            int lastRowNum = sheet.getLastRowNum();

            if (lastRowNum < 1) {
                result.addErrorMessage("Excel 文件没有可导入的数据");
                result.setTotalCount(0);
                return result;
            }

            // ---------- 逐行读取 ----------

            // 从第1行开始（第0行是表头），所以 i 从 1 开始
            for (int i = 1; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);

                // 跳过空行
                if (row == null) {
                    continue;
                }

                // --- 读取单元格 ---
                // 列顺序：学号(0) | 姓名(1) | 性别(2) | 学院(3) | 年级(4) | 专业(5) | 班级(6)
                String studentNo = ExcelUtils.getCellStringValue(row.getCell(0));
                String name = ExcelUtils.getCellStringValue(row.getCell(1));
                String gender = ExcelUtils.getCellStringValue(row.getCell(2));
                String college = ExcelUtils.getCellStringValue(row.getCell(3));
                String grade = ExcelUtils.getCellStringValue(row.getCell(4));
                String major = ExcelUtils.getCellStringValue(row.getCell(5));
                String className = ExcelUtils.getCellStringValue(row.getCell(6));

                // trim() 去掉首尾空格，防止用户不小心打了空格
                studentNo = (studentNo == null) ? null : studentNo.trim();
                name = (name == null) ? null : name.trim();
                gender = (gender == null) ? null : gender.trim();
                college = (college == null) ? null : college.trim();
                grade = (grade == null) ? null : grade.trim();
                major = (major == null) ? null : major.trim();
                className = (className == null) ? null : className.trim();

                // --- 判断是否为空行 ---
                // 如果学号、姓名、性别、班级全是空的，就认为这一行没有数据，跳过
                if (isEmptyRow(studentNo, name, gender, className)) {
                    continue;
                }

                // 总记录数 +1
                result.setTotalCount(result.getTotalCount() + 1);

                // --- 字段合法性校验 ---
                try {
                    // 每个校验不通过都用 continue 跳出当前行，进入下一行

                    if (studentNo == null || studentNo.isEmpty()) {
                        result.addErrorMessage("第 " + (i + 1) + " 行：学号不能为空");
                        continue;
                    }

                    if (name == null || name.isEmpty()) {
                        result.addErrorMessage("第 " + (i + 1) + " 行：姓名不能为空");
                        continue;
                    }

                    if (gender == null || gender.isEmpty()) {
                        result.addErrorMessage("第 " + (i + 1) + " 行：性别不能为空");
                        continue;
                    }

                    if (className == null || className.isEmpty()) {
                        result.addErrorMessage("第 " + (i + 1) + " 行：班级不能为空");
                        continue;
                    }

                    // 性别只能是"男"或"女"
                    if (!"男".equals(gender) && !"女".equals(gender)) {
                        result.addErrorMessage("第 " + (i + 1) + " 行：性别只能是“男”或“女”，你填的是“" + gender + "”");
                        continue;
                    }

                    // 检查 Excel 内部是否有重复学号
                    if (excelStudentNoSet.contains(studentNo)) {
                        result.addErrorMessage("第 " + (i + 1) + " 行：学号在 Excel 中重复 -> " + studentNo);
                        continue;
                    }

                    // 检查数据库中是否已存在该学号
                    if (studentRepository.existsByStudentNo(studentNo)) {
                        result.addErrorMessage("第 " + (i + 1) + " 行：学号已存在于数据库 -> " + studentNo);
                        continue;
                    }

                    // --- 所有校验通过，创建学生对象并保存 ---
                    Student student = new Student();
                    student.setStudentNo(studentNo);
                    student.setName(name);
                    student.setGender(gender);
                    student.setCollege(college);   // 学院（可选）
                    student.setGrade(grade);       // 年级（可选）
                    student.setMajor(major);       // 专业（可选）
                    student.setClassName(className);
                    student.setStatus(1);          // 默认启用

                    // save() 方法执行 INSERT 语句
                    studentRepository.save(student);

                    // 记录已导入的学号，防止同一份 Excel 里重复
                    excelStudentNoSet.add(studentNo);
                    result.setSuccessCount(result.getSuccessCount() + 1);

                } catch (Exception e) {
                    // 捕获意料之外的异常（如数据库连接断开），不至于让整个导入崩溃
                    result.addErrorMessage("第 " + (i + 1) + " 行导入失败：" + e.getMessage());
                }
            }

        } catch (Exception e) {
            // 文件解析失败（比如文件损坏、不是真正的 Excel 文件）
            result.addErrorMessage("Excel 文件解析失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 辅助方法：判断一行是否为空
     * 如果关键的几个字段都为空，就认为这一行没有有效数据
     */
    private boolean isEmptyRow(String studentNo, String name, String gender, String className) {
        return (studentNo == null || studentNo.isEmpty())
                && (name == null || name.isEmpty())
                && (gender == null || gender.isEmpty())
                && (className == null || className.isEmpty());
    }
}
