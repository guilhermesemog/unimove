package com.guilhermesemog.unimove.service;

import java.util.UUID;
import com.guilhermesemog.unimove.dto.student.StudentCreate;
import com.guilhermesemog.unimove.dto.student.StudentPatch;
import com.guilhermesemog.unimove.dto.student.StudentResponse;
import com.guilhermesemog.unimove.dto.student.StudentUpdate;
import com.guilhermesemog.unimove.dto.student.StudentPreferredBoardingStopUpdate;
import com.guilhermesemog.unimove.exception.type.ResourceNotFoundException;
import com.guilhermesemog.unimove.mapper.StudentMapper;
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
import org.springframework.security.core.Authentication;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;
    private final UniversityRepository universityRepository;
    private final BoardingStopRepository boardingStopRepository;
    private final AuthService authService;
    private final UserValidationService userValidationService;

    public StudentService(StudentRepository studentRepository, StudentMapper studentMapper, UniversityRepository universityRepository, BoardingStopRepository boardingStopRepository, AuthService authService, UserValidationService userValidationService) {
        this.studentRepository = studentRepository;
        this.studentMapper = studentMapper;
        this.universityRepository = universityRepository;
        this.boardingStopRepository = boardingStopRepository;
        this.authService = authService;
        this.userValidationService = userValidationService;
    }

    public StudentResponse create(StudentCreate requestBody) {
        University university = getUniversity(requestBody.universityId());
        BoardingStop boardingStop = getBoardingStop(requestBody.preferredBoardingStopId());

        User user = authService.createAuthenticatableUser(requestBody.user().cpf(), requestBody.user().password(), Role.STUDENT);
        user.setFirstName(requestBody.user().firstName());
        user.setLastName(requestBody.user().lastName());
        user.setPhone(requestBody.user().phone());

        Student student = studentMapper.toEntity(requestBody, user, university, boardingStop);

        return studentMapper.toResponse(studentRepository.save(student));
    }

    public StudentResponse getById(UUID id) {
        return studentMapper.toResponse(getStudent(id));
    }

    public Page<StudentResponse> getAll(int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return studentRepository.findAll(pageable).map(studentMapper::toResponse);
    }


    public void delete(UUID id) {
        studentRepository.delete(getStudent(id));
    }

    public void update(UUID id, StudentUpdate requestBody) {
        Student student = getStudent(id);
        University university = getUniversity(requestBody.universityId());

        this.userValidationService.validateCpf(id, requestBody.user().cpf());

        student = studentMapper.update(requestBody, student, university, getBoardingStop(requestBody.preferredBoardingStopId()));
        studentRepository.save(student);
    }

    public void update(UUID id, StudentPatch requestBody) {
        Student student = getStudent(id);
        University university = getUniversity(requestBody.universityId());

        this.userValidationService.validateCpf(id, requestBody.user().cpf());

        student = studentMapper.update(requestBody, student, university, getBoardingStop(requestBody.preferredBoardingStopId()));
        studentRepository.save(student);
    }

    public StudentResponse updatePreferredBoardingStop(Authentication authentication, StudentPreferredBoardingStopUpdate requestBody) {
        Student student = studentRepository.findByUser_Cpf(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        student.setPreferredBoardingStop(getBoardingStop(requestBody.boardingStopId()));
        return studentMapper.toResponse(studentRepository.save(student));
    }

    private Student getStudent(UUID id) {
        return studentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    }

    private University getUniversity(UUID id) {
        return universityRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("University not found"));
    }

    private BoardingStop getBoardingStop(UUID id) {
        if (id == null) {
            return null;
        }

        return boardingStopRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Boarding stop not found"));
    }


}
