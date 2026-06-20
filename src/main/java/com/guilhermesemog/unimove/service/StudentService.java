package com.guilhermesemog.unimove.service;

import com.guilhermesemog.unimove.dto.student.StudentPatchRequestBody;
import com.guilhermesemog.unimove.dto.student.StudentPostRequestBody;
import com.guilhermesemog.unimove.dto.student.StudentPutRequestBody;
import com.guilhermesemog.unimove.dto.student.StudentResponseBody;
import com.guilhermesemog.unimove.exception.ResourceNotFoundException;
import com.guilhermesemog.unimove.mapper.StudentMapper;
import com.guilhermesemog.unimove.mapper.UserMapper;
import com.guilhermesemog.unimove.model.BoardingStop;
import com.guilhermesemog.unimove.model.Student;
import com.guilhermesemog.unimove.model.University;
import com.guilhermesemog.unimove.model.User;
import com.guilhermesemog.unimove.model.enums.Role;
import com.guilhermesemog.unimove.repository.BoardingStopRepository;
import com.guilhermesemog.unimove.repository.StudentRepository;
import com.guilhermesemog.unimove.repository.UniversityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;
    private final UserMapper userMapper;
    private final UniversityRepository universityRepository;
    private final BoardingStopRepository boardingStopRepository;

    public StudentService(StudentRepository studentRepository, StudentMapper studentMapper, UserMapper userMapper, UniversityRepository universityRepository, BoardingStopRepository boardingStopRepository) {
        this.studentRepository = studentRepository;
        this.studentMapper = studentMapper;
        this.userMapper = userMapper;
        this.universityRepository = universityRepository;
        this.boardingStopRepository = boardingStopRepository;
    }

    public StudentResponseBody create(StudentPostRequestBody studentPostRequestBody) {
        University university = universityRepository.findById(studentPostRequestBody.universityId())
                .orElseThrow(() -> new ResourceNotFoundException("University not found"));

        BoardingStop boardingStop = boardingStopRepository.findById(studentPostRequestBody.preferredBoardingStopId())
                .orElseThrow(() -> new ResourceNotFoundException("Boarding stop not found"));

        User user = userMapper.toEntity(studentPostRequestBody.user(), Role.STUDENT);

        Student student = studentMapper.toEntity(studentPostRequestBody, user, university, boardingStop);
        return studentMapper.toResponseBody(studentRepository.save(student));
    }

    public StudentResponseBody getById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        return studentMapper.toResponseBody(student);
    }

    public Page<StudentResponseBody> getAll(int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return studentRepository.findAll(pageable).map(studentMapper::toResponseBody);
    }


    public void delete(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        studentRepository.delete(student);
    }

    public void update(Long id, StudentPutRequestBody studentPutRequestBody) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        University university = universityRepository.findById(studentPutRequestBody.universityId())
                .orElseThrow(() -> new ResourceNotFoundException("University not found"));

        if (studentPutRequestBody.preferredBoardingStopId() == null) {
            student = studentMapper.update(studentPutRequestBody, student, university, null);
            studentRepository.save(student);
            return;
        }

        BoardingStop boardingStop = boardingStopRepository.findById(studentPutRequestBody.preferredBoardingStopId())
                .orElseThrow(() -> new ResourceNotFoundException("Boarding stop not found"));

        student = studentMapper.update(studentPutRequestBody, student, university, boardingStop);
        studentRepository.save(student);
    }

    public void update(Long id, StudentPatchRequestBody studentPatchRequestBody) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        University university = universityRepository.findById(studentPatchRequestBody.universityId())
                .orElseThrow(() -> new ResourceNotFoundException("University not found"));

        if (studentPatchRequestBody.preferredBoardingStopId() == null) {
            student = studentMapper.update(studentPatchRequestBody, student, university, null);
            studentRepository.save(student);
            return;
        }

        BoardingStop boardingStop = boardingStopRepository.findById(studentPatchRequestBody.preferredBoardingStopId())
                .orElseThrow(() -> new ResourceNotFoundException("Boarding stop not found"));

        student = studentMapper.update(studentPatchRequestBody, student, university, boardingStop);
        studentRepository.save(student);
    }

}
