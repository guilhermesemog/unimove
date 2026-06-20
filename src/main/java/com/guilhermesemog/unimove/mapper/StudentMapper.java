package com.guilhermesemog.unimove.mapper;

import com.guilhermesemog.unimove.dto.student.StudentCreate;
import com.guilhermesemog.unimove.dto.student.StudentPatch;
import com.guilhermesemog.unimove.dto.student.StudentResponse;
import com.guilhermesemog.unimove.dto.student.StudentUpdate;
import com.guilhermesemog.unimove.model.BoardingStop;
import com.guilhermesemog.unimove.model.Student;
import com.guilhermesemog.unimove.model.University;
import com.guilhermesemog.unimove.model.User;
import org.springframework.stereotype.Component;

@Component
public class StudentMapper {

    private final UserMapper userMapper;

    public StudentMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public Student toEntity(StudentCreate studentBody, User user, University university, BoardingStop boardingStop) {
        return new Student(
                user,
                studentBody.period(),
                studentBody.course(),
                studentBody.address(),
                university,
                boardingStop
        );
    }

    public StudentResponse toResponseBody(Student student) {
        return new StudentResponse(
                userMapper.toResponseBody(student.getUser()),
                student.getPeriod(),
                student.getCourse(),
                student.getAddress(),
                student.getUniversity().getName(),
                student.getPreferredBoardingStop() != null ? student.getPreferredBoardingStop().getLocal() : null
        );
    }

    public Student update(StudentUpdate newStudent, Student student, University university, BoardingStop boardingStop) {
        student.setUser(userMapper.updateUser(newStudent.user(), student.getUser()));
        student.setPeriod(newStudent.period());
        student.setCourse(newStudent.course());
        student.setAddress(newStudent.address());
        student.setUniversity(university);
        student.setPreferredBoardingStop(boardingStop);

        return student;
    }

    public Student update(StudentPatch newStudent, Student student, University university, BoardingStop boardingStop) {

        if (newStudent.user() != null) {
            student.setUser(userMapper.updateUser(newStudent.user(), student.getUser()));
        }

        if (newStudent.period() != null) {
            student.setPeriod(newStudent.period());
        }
        if (newStudent.course() != null) {
            student.setCourse(newStudent.course());
        }
        if (newStudent.address() != null) {
            student.setAddress(newStudent.address());
        }
        if (newStudent.universityId() != null) {
            student.setUniversity(university);
        }

        Long bsId = newStudent.preferredBoardingStopId();

        if (bsId != null) {
            if (bsId == -1) {
                student.setPreferredBoardingStop(null);
            } else {
                student.setPreferredBoardingStop(boardingStop);
            }
        }

        return student;
    }
}
