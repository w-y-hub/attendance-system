package com.example.attendance.service;

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
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    // 新增或保存学生
    public void save(Student student) {
        studentRepository.save(student);
    }

    public Student findById(Long id) {
        return studentRepository.findById(id).orElse(null);
    }

    // 查询所有
    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    // 更新学生
    public void update(Student student) {
        Student oldStudent = studentRepository.findById(student.getId()).orElse(null);
        if (oldStudent != null) {
            oldStudent.setStudentNo(student.getStudentNo());
            oldStudent.setName(student.getName());
            oldStudent.setClassName(student.getClassName());
            oldStudent.setGender(student.getGender());
            oldStudent.setBirthDate(student.getBirthDate());
            oldStudent.setPhone(student.getPhone());
            oldStudent.setEmail(student.getEmail());
            oldStudent.setStatus(student.getStatus());
            studentRepository.save(oldStudent);
        }
    }

    // 根据主键 id 删除
    public void deleteById(Long id) {
        studentRepository.deleteById(id);
    }

    // 根据学号删除
    public void deleteByStudentNo(String studentNo) {
        Student student = studentRepository.findByStudentNo(studentNo);
        if (student != null) {
            studentRepository.deleteById(student.getId());
        }
    }

    // 根据班级查询
    public List<Student> getByClassName(String className) {
        return studentRepository.findByClassName(className);
    }

    // 学号是否存在
    public boolean existsByStudentNo(String studentNo) {
        return studentRepository.existsByStudentNo(studentNo);
    }

    public Student findByStudentNo(String studentNo) {
        return studentRepository.findByStudentNo(studentNo);
    }

    public StudentImportResult importStudents(MultipartFile file) {
        StudentImportResult result = new StudentImportResult();
        Set<String> excelStudentNoSet = new HashSet<>();

        if (file == null || file.isEmpty()) {
            result.addErrorMessage("上传文件不能为空");
            return result;
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".xlsx")) {
            result.addErrorMessage("请上传 .xlsx 格式的 Excel 文件");
            return result;
        }

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();

            if (lastRowNum < 1) {
                result.addErrorMessage("Excel 文件没有可导入的数据");
                result.setTotalCount(0);
                return result;
            }

            for (int i = 1; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);

                if (row == null) {
                    continue;
                }

                String studentNo = ExcelUtils.getCellStringValue(row.getCell(0));
                String name = ExcelUtils.getCellStringValue(row.getCell(1));
                String gender = ExcelUtils.getCellStringValue(row.getCell(2));
                String className = ExcelUtils.getCellStringValue(row.getCell(6));

                studentNo = studentNo == null ? null : studentNo.trim();
                name = name == null ? null : name.trim();
                gender = gender == null ? null : gender.trim();
                className = className == null ? null : className.trim();

                // 整行关键字段都为空，视为空行
                if ((studentNo == null || studentNo.isEmpty())
                        && (name == null || name.isEmpty())
                        && (gender == null || gender.isEmpty())
                        && (className == null || className.isEmpty())) {
                    continue;
                }

                result.setTotalCount(result.getTotalCount() + 1);

                try {
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

                    if (!"男".equals(gender) && !"女".equals(gender)) {
                        result.addErrorMessage("第 " + (i + 1) + " 行：性别只能是“男”或“女”");
                        continue;
                    }

                    if (excelStudentNoSet.contains(studentNo)) {
                        result.addErrorMessage("第 " + (i + 1) + " 行：学号在 Excel 中重复 -> " + studentNo);
                        continue;
                    }

                    if (studentRepository.existsByStudentNo(studentNo)) {
                        result.addErrorMessage("第 " + (i + 1) + " 行：学号已存在于数据库 -> " + studentNo);
                        continue;
                    }

                    Student student = new Student();
                    student.setStudentNo(studentNo);
                    student.setName(name);
                    student.setGender(gender);
                    student.setClassName(className);
                    student.setBirthDate(null);
                    student.setPhone(null);
                    student.setEmail(null);
                    student.setStatus(1);

                    studentRepository.save(student);

                    excelStudentNoSet.add(studentNo);
                    result.setSuccessCount(result.getSuccessCount() + 1);

                } catch (Exception e) {
                    result.addErrorMessage("第 " + (i + 1) + " 行导入失败");
                }
            }

        } catch (Exception e) {
            result.addErrorMessage("Excel 文件解析失败：" + e.getMessage());
        }

        return result;
    }
}
