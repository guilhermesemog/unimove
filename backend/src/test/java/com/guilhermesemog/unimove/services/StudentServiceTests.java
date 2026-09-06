package com.guilhermesemog.unimove.services;

import java.util.UUID;
import com.guilhermesemog.unimove.dto.student.StudentPreferredBoardingStopUpdate;
import com.guilhermesemog.unimove.dto.student.StudentResponse;
import com.guilhermesemog.unimove.mapper.StudentMapper;
import com.guilhermesemog.unimove.model.BoardingStop;
import com.guilhermesemog.unimove.model.Student;
import com.guilhermesemog.unimove.repository.BoardingStopRepository;
import com.guilhermesemog.unimove.repository.StudentRepository;
import com.guilhermesemog.unimove.repository.UniversityRepository;
import com.guilhermesemog.unimove.service.AuthService;
import com.guilhermesemog.unimove.service.StudentService;
import com.guilhermesemog.unimove.service.UserValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentService Tests")
class StudentServiceTests {

    @Mock private StudentRepository studentRepository;
    @Mock private StudentMapper studentMapper;
    @Mock private UniversityRepository universityRepository;
    @Mock private BoardingStopRepository boardingStopRepository;
    @Mock private AuthService authService;
    @Mock private UserValidationService userValidationService;
    @Mock private Authentication authentication;
    @Mock private StudentResponse studentResponse;

    private StudentService studentService;

    @BeforeEach
    void setUp() {
        studentService = new StudentService(
                studentRepository,
                studentMapper,
                universityRepository,
                boardingStopRepository,
                authService,
                userValidationService
        );
    }

    @Test
    @DisplayName("should update only the authenticated student's preferred boarding stop")
    void shouldUpdateAuthenticatedStudentPreferredBoardingStop() {
        Student student = new Student();
        BoardingStop boardingStop = new BoardingStop();
        boardingStop.setId(UUID.fromString("00000000-0000-4000-8000-000000000005"));

        given(authentication.getName()).willReturn("12345678900");
        given(studentRepository.findByUser_Cpf("12345678900")).willReturn(Optional.of(student));
        given(boardingStopRepository.findById(UUID.fromString("00000000-0000-4000-8000-000000000005"))).willReturn(Optional.of(boardingStop));
        given(studentRepository.save(student)).willReturn(student);
        given(studentMapper.toResponse(student)).willReturn(studentResponse);

        StudentResponse response = studentService.updatePreferredBoardingStop(
                authentication,
                new StudentPreferredBoardingStopUpdate(UUID.fromString("00000000-0000-4000-8000-000000000005"))
        );

        assertThat(student.getPreferredBoardingStop()).isSameAs(boardingStop);
        assertThat(response).isSameAs(studentResponse);
        verify(studentRepository).save(student);
    }
}
