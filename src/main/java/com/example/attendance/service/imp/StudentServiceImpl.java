package com.example.attendance.service.imp;
import com.example.attendance.dao.StudentDao;
import com.example.attendance.entity.Student;
import com.example.attendance.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentServiceImpl implements StudentService{
    @Autowired
    private StudentDao studentDao;

    @Override
    public String addStudent(Student student) {
        if (student.getStudentName() == null || student.getStudentName().isEmpty()) {
            return "姓名不能为空！";
        }
        studentDao.Insert(student);
        return "创建成功";
    }

    @Override
    public Student getById(Long id) {
        return studentDao.findById(id);
    }

    @Override
    public List<Student> findAll() {
        return studentDao.findAll();
    }

    @Override
    public void update(Student student) {
        studentDao.update(student);
    }

    @Override
    public void deleteByStudentId(String studentId) {
        studentDao.deleteByStudentId(studentId);
    }
}
