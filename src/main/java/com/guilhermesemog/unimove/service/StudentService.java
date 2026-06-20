package com.guilhermesemog.unimove.service;

import com.guilhermesemog.unimove.dto.student.StudentCreate;
import com.guilhermesemog.unimove.dto.student.StudentPatch;
import com.guilhermesemog.unimove.dto.student.StudentResponse;
import com.guilhermesemog.unimove.dto.student.StudentUpdate;
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

    public StudentResponse create(StudentCreate requestBody) {
        University university = getUniversity(requestBody.universityId());
        BoardingStop boardingStop = getBoardingStop(requestBody.preferredBoardingStopId());

        User user = userMapper.toEntity(requestBody.user(), Role.STUDENT);
        Student student = studentMapper.toEntity(requestBody, user, university, boardingStop);

        return studentMapper.toResponseBody(studentRepository.save(student));
    }

    public StudentResponse getById(Long id) {
        return studentMapper.toResponseBody(getStudent(id));
    }

    public Page<StudentResponse> getAll(int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return studentRepository.findAll(pageable).map(studentMapper::toResponseBody);
    }


    public void delete(Long id) {
        studentRepository.delete(getStudent(id));
    }

    public void update(Long id, StudentUpdate requestBody) {
        Student student = getStudent(id);
        University university = getUniversity(requestBody.universityId());

        student = studentMapper.update(requestBody, student, university, getBoardingStop(requestBody.preferredBoardingStopId()));

        studentRepository.save(student);
    }

    public void update(Long id, StudentPatch requestBody) {
        Student student = getStudent(id);
        University university = getUniversity(requestBody.universityId());

        student = studentMapper.update(requestBody, student, university, getBoardingStop(requestBody.preferredBoardingStopId()));

        studentRepository.save(student);
    }

    private Student getStudent(Long id) {
        return studentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    }

    private University getUniversity(Long id) {
        return universityRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("University not found"));
    }

    private BoardingStop getBoardingStop(Long id) {
        if (id == null) {
            return null;
        }

        return boardingStopRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Boarding stop not found"));
    }


}
