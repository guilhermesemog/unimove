package com.guilhermesemog.unimove.services;

import com.guilhermesemog.unimove.dto.booking.BookingCreate;
import com.guilhermesemog.unimove.exception.type.IllegalUpdateException;
import com.guilhermesemog.unimove.mapper.BookingMapper;
import com.guilhermesemog.unimove.model.InterestList;
import com.guilhermesemog.unimove.model.Student;
import com.guilhermesemog.unimove.model.enums.ListStatus;
import com.guilhermesemog.unimove.model.enums.TripType;
import com.guilhermesemog.unimove.repository.BoardingStopRepository;
import com.guilhermesemog.unimove.repository.BookingRepository;
import com.guilhermesemog.unimove.repository.InterestListRepository;
import com.guilhermesemog.unimove.repository.StudentRepository;
import com.guilhermesemog.unimove.repository.UniversityRepository;
import com.guilhermesemog.unimove.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService Tests")
class BookingServiceTests {

    @Mock private BookingMapper bookingMapper;
    @Mock private BookingRepository bookingRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private BoardingStopRepository boardingStopRepository;
    @Mock private InterestListRepository interestListRepository;
    @Mock private UniversityRepository universityRepository;
    @Mock private Authentication authentication;

    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new BookingService(
                bookingMapper,
                bookingRepository,
                studentRepository,
                boardingStopRepository,
                interestListRepository,
                universityRepository
        );
    }

    @Test
    @DisplayName("should reject a booking when the interest list is closed")
    void shouldRejectBookingForClosedInterestList() {
        Student student = new Student();
        student.setId(7L);
        InterestList interestList = new InterestList();
        interestList.setId(13L);
        interestList.setListStatus(ListStatus.CLOSED);
        BookingCreate request = new BookingCreate(13L, TripType.ROUND_TRIP, null, null);

        given(authentication.getName()).willReturn("12345678900");
        given(studentRepository.findByUser_Cpf("12345678900")).willReturn(Optional.of(student));
        given(interestListRepository.findById(13L)).willReturn(Optional.of(interestList));

        assertThatThrownBy(() -> bookingService.create(authentication, request))
                .isInstanceOf(IllegalUpdateException.class)
                .hasMessage("This interest list is no longer accepting bookings");

        verify(bookingRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("should reject cancellation after the interest list closes")
    void shouldRejectCancellationForClosedInterestList() {
        InterestList interestList = new InterestList();
        interestList.setListStatus(ListStatus.CLOSED);
        var booking = new com.guilhermesemog.unimove.model.Booking();
        booking.setInterestList(interestList);

        given(bookingRepository.findById(21L)).willReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.deleteById(21L))
                .isInstanceOf(IllegalUpdateException.class)
                .hasMessage("Cannot cancel a booking after the interest list closes");

        verify(bookingRepository, never()).delete(booking);
    }

    @Test
    @DisplayName("should delete and flush an open-list booking")
    void shouldDeleteAndFlushOpenListBooking() {
        InterestList interestList = new InterestList();
        interestList.setListStatus(ListStatus.OPEN);
        var booking = new com.guilhermesemog.unimove.model.Booking();
        booking.setInterestList(interestList);

        given(bookingRepository.findById(21L)).willReturn(Optional.of(booking));

        bookingService.deleteById(21L);

        verify(bookingRepository).delete(booking);
        verify(bookingRepository).flush();
    }
}
