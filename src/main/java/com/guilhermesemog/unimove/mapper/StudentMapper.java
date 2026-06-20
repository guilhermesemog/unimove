package com.guilhermesemog.unimove.mapper;

import com.guilhermesemog.unimove.dto.student.StudentPatchRequestBody;
import com.guilhermesemog.unimove.dto.student.StudentPostRequestBody;
import com.guilhermesemog.unimove.dto.student.StudentPutRequestBody;
import com.guilhermesemog.unimove.dto.student.StudentResponseBody;
import com.guilhermesemog.unimove.dto.user.UserResponseBody;
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

    public Student toEntity(StudentPostRequestBody studentBody, User user, University university, BoardingStop boardingStop) {
        return new Student(
                user,
                studentBody.period(),
                studentBody.course(),
                studentBody.address(),
                university,
                boardingStop
        );
    }

    public StudentResponseBody toResponseBody(Student student) {
        UserResponseBody userResponseBody = userMapper.toResponseBody(student.getUser());
        return new StudentResponseBody(
                userResponseBody,
                student.getPeriod(),
                student.getCourse(),
                student.getAddress(),
                student.getUniversity().getName(),
                student.getPreferredBoardingStop() != null ? student.getPreferredBoardingStop().getLocal() : null
        );
    }

    public Student update(StudentPutRequestBody newStudent, Student student, University university, BoardingStop boardingStop) {
        student.setUser(userMapper.updateUser(newStudent.user(), student.getUser()));
        student.setPeriod(newStudent.period());
        student.setCourse(newStudent.course());
        student.setAddress(newStudent.address());
        student.setUniversity(university);
        student.setPreferredBoardingStop(boardingStop);

        return student;
    }

    public Student update(StudentPatchRequestBody newStudent, Student student, University university, BoardingStop boardingStop) {

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

        Long newBoardingStopId = newStudent.preferredBoardingStopId();

        if (newBoardingStopId != null) {
            if (newBoardingStopId == -1) {
                student.setPreferredBoardingStop(null);
            } else {
                student.setPreferredBoardingStop(boardingStop);
            }
        }

        return student;
    }
}
