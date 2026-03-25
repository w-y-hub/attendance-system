package com.example.attendance.service.imp;
import com.example.attendance.dao.StudentDao;
import com.example.attendance.entity.Student;
import com.example.attendance.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

}
