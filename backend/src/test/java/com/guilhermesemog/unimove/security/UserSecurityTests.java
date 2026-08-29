package com.guilhermesemog.unimove.security;

import com.guilhermesemog.unimove.model.Booking;
import com.guilhermesemog.unimove.model.Student;
import com.guilhermesemog.unimove.model.User;
import com.guilhermesemog.unimove.model.enums.Role;
import com.guilhermesemog.unimove.repository.BookingRepository;
import com.guilhermesemog.unimove.repository.UserRepository;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("UserSecurity Tests")
class UserSecurityTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private Authentication authentication;

    private UserSecurity userSecurity;

    @BeforeEach
    void setUp() {
        userSecurity = new UserSecurity(userRepository, bookingRepository);
    }

    @Test
    @DisplayName("should allow the student who owns the booking")
    void shouldAllowBookingOwner() {
        given(authentication.getName()).willReturn("12345678900");
        given(bookingRepository.findById(21L)).willReturn(Optional.of(bookingFor("12345678900")));

        assertThat(userSecurity.isBookingOwner(21L, authentication)).isTrue();
    }

    @Test
    @DisplayName("should reject a different student")
    void shouldRejectDifferentStudent() {
        given(authentication.getName()).willReturn("99999999999");
        given(bookingRepository.findById(21L)).willReturn(Optional.of(bookingFor("12345678900")));

        assertThat(userSecurity.isBookingOwner(21L, authentication)).isFalse();
    }

    @Test
    @DisplayName("should reject when the booking does not exist")
    void shouldRejectMissingBooking() {
        given(bookingRepository.findById(21L)).willReturn(Optional.empty());

        assertThat(userSecurity.isBookingOwner(21L, authentication)).isFalse();
    }

    private Booking bookingFor(String cpf) {
        User user = new User(cpf, "password", Role.STUDENT);
        Student student = new Student();
        student.setUser(user);
        Booking booking = new Booking();
        booking.setStudent(student);
        return booking;
    }
}
