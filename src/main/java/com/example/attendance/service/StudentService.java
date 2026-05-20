package com.example.attendance.service;

import com.example.attendance.entity.Student;
import com.example.attendance.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    // 新增或保存学生
    public void save(Student student) {
        studentRepository.save(student);
    }

    // 根据 id 查询
    public Student getById(Long id) {
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

    // 根据学号查询
    public Student getByStudentNo(String studentNo) {
        return studentRepository.findByStudentNo(studentNo);
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
}