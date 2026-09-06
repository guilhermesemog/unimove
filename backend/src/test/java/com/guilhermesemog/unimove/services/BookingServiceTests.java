package com.guilhermesemog.unimove.services;

import java.util.UUID;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import com.guilhermesemog.unimove.dto.booking.BookingCreate;
import com.guilhermesemog.unimove.exception.type.IllegalUpdateException;
import com.guilhermesemog.unimove.mapper.BookingMapper;
import com.guilhermesemog.unimove.model.InterestList;
import com.guilhermesemog.unimove.model.Student;
import com.guilhermesemog.unimove.model.Booking;
import com.guilhermesemog.unimove.model.BoardingStop;
import com.guilhermesemog.unimove.model.University;
import com.guilhermesemog.unimove.model.enums.BookingStatus;
import com.guilhermesemog.unimove.model.enums.AuditAction;
import com.guilhermesemog.unimove.model.enums.BusinessEventType;
import com.guilhermesemog.unimove.model.enums.ListStatus;
import com.guilhermesemog.unimove.model.enums.TripType;
import com.guilhermesemog.unimove.repository.BoardingStopRepository;
import com.guilhermesemog.unimove.repository.BookingRepository;
import com.guilhermesemog.unimove.repository.InterestListRepository;
import com.guilhermesemog.unimove.repository.StudentRepository;
import com.guilhermesemog.unimove.repository.UniversityRepository;
import com.guilhermesemog.unimove.service.BookingService;
import com.guilhermesemog.unimove.service.BusinessEventPublisher;
import com.guilhermesemog.unimove.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService Tests")
class BookingServiceTests {

    private static final Clock BUSINESS_CLOCK = Clock.fixed(
            Instant.parse("2026-09-01T12:00:00Z"),
            ZoneId.of("America/Sao_Paulo")
    );

    @Mock private BookingMapper bookingMapper;
    @Mock private BookingRepository bookingRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private BoardingStopRepository boardingStopRepository;
    @Mock private InterestListRepository interestListRepository;
    @Mock private UniversityRepository universityRepository;
    @Mock private Authentication authentication;
    @Mock private BusinessEventPublisher eventPublisher;
    @Mock private AuditService auditService;

    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new BookingService(
                bookingMapper,
                bookingRepository,
                studentRepository,
                boardingStopRepository,
                interestListRepository,
                universityRepository,
                eventPublisher,
                auditService,
                BUSINESS_CLOCK
        );
    }

    @Test
    @DisplayName("should reject a booking when the interest list is closed")
    void shouldRejectBookingForClosedInterestList() {
        Student student = new Student();
        student.setId(UUID.fromString("00000000-0000-4000-8000-000000000007"));
        InterestList interestList = new InterestList();
        interestList.setId(UUID.fromString("00000000-0000-4000-8000-000000000013"));
        interestList.setListStatus(ListStatus.CLOSED);
        BookingCreate request = new BookingCreate(UUID.fromString("00000000-0000-4000-8000-000000000013"), TripType.ROUND_TRIP, null, null);

        given(authentication.getName()).willReturn("12345678900");
        given(studentRepository.findByUser_Cpf("12345678900")).willReturn(Optional.of(student));
        given(interestListRepository.findById(UUID.fromString("00000000-0000-4000-8000-000000000013"))).willReturn(Optional.of(interestList));

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

        given(bookingRepository.findById(UUID.fromString("00000000-0000-4000-8000-000000000021"))).willReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.deleteById(UUID.fromString("00000000-0000-4000-8000-000000000021")))
                .isInstanceOf(IllegalUpdateException.class)
                .hasMessage("Cannot cancel a booking after the interest list closes");

        verify(bookingRepository, never()).delete(booking);
    }

    @Test
    @DisplayName("should preserve and cancel an open-list booking")
    void shouldPreserveCancelledOpenListBooking() {
        InterestList interestList = new InterestList();
        interestList.setId(UUID.fromString("00000000-0000-4000-8000-000000000013"));
        interestList.setListStatus(ListStatus.OPEN);
        interestList.setReferenceDate(LocalDate.of(2026, 9, 2));
        interestList.setClosingTime(LocalTime.of(8, 0));
        Student student = new Student();
        student.setId(UUID.fromString("00000000-0000-4000-8000-000000000007"));
        Booking booking = new Booking();
        booking.setId(UUID.fromString("00000000-0000-4000-8000-000000000021"));
        booking.setStudent(student);
        booking.setInterestList(interestList);
        booking.setBookingStatus(BookingStatus.APPROVED);

        given(bookingRepository.findById(UUID.fromString("00000000-0000-4000-8000-000000000021"))).willReturn(Optional.of(booking));
        given(bookingRepository.saveAndFlush(booking)).willReturn(booking);

        bookingService.deleteById(UUID.fromString("00000000-0000-4000-8000-000000000021"));

        assertThat(booking.getBookingStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(booking.getCancelledAt()).isNotNull();
        verify(bookingRepository).saveAndFlush(booking);
        verify(eventPublisher).publish(
                eq(BusinessEventType.BOOKING_CANCELLED),
                eq("Booking"),
                eq(UUID.fromString("00000000-0000-4000-8000-000000000021")),
                any(),
                eq("BOOKING_CANCELLED:00000000-0000-4000-8000-000000000021:0")
        );
        verify(auditService).record(
                eq(AuditAction.BOOKING_CANCELLED),
                eq("Booking"),
                eq(UUID.fromString("00000000-0000-4000-8000-000000000021")),
                any(),
                any(),
                any()
        );
    }

    @Test
    @DisplayName("should reactivate a cancelled booking without creating a duplicate")
    void shouldReactivateCancelledBooking() {
        Student student = new Student();
        student.setId(UUID.fromString("00000000-0000-4000-8000-000000000007"));
        University university = new University();
        university.setId(UUID.fromString("00000000-0000-4000-8000-000000000005"));
        BoardingStop boardingStop = new BoardingStop();
        boardingStop.setId(UUID.fromString("00000000-0000-4000-8000-000000000003"));
        student.setUniversity(university);
        student.setPreferredBoardingStop(boardingStop);

        InterestList interestList = new InterestList();
        interestList.setId(UUID.fromString("00000000-0000-4000-8000-000000000013"));
        interestList.setListStatus(ListStatus.OPEN);
        interestList.setReferenceDate(LocalDate.of(2026, 9, 2));
        interestList.setClosingTime(LocalTime.of(8, 0));

        Booking booking = new Booking();
        booking.setId(UUID.fromString("00000000-0000-4000-8000-000000000021"));
        booking.setStudent(student);
        booking.setInterestList(interestList);
        booking.setBookingStatus(BookingStatus.CANCELLED);

        BookingCreate request = new BookingCreate(UUID.fromString("00000000-0000-4000-8000-000000000013"), TripType.ROUND_TRIP, null, null);
        given(authentication.getName()).willReturn("12345678900");
        given(studentRepository.findByUser_Cpf("12345678900")).willReturn(Optional.of(student));
        given(interestListRepository.findById(UUID.fromString("00000000-0000-4000-8000-000000000013"))).willReturn(Optional.of(interestList));
        given(bookingRepository.findByStudent_IdAndInterestList_Id(UUID.fromString("00000000-0000-4000-8000-000000000007"), UUID.fromString("00000000-0000-4000-8000-000000000013"))).willReturn(Optional.of(booking));
        given(bookingRepository.save(booking)).willReturn(booking);

        bookingService.create(authentication, request);

        assertThat(booking.getBookingStatus()).isEqualTo(BookingStatus.APPROVED);
        assertThat(booking.getCancelledAt()).isNull();
        assertThat(booking.getTripType()).isEqualTo(TripType.ROUND_TRIP);
        verify(bookingRepository).save(booking);
        verify(eventPublisher).publish(
                eq(BusinessEventType.BOOKING_REACTIVATED),
                eq("Booking"),
                eq(UUID.fromString("00000000-0000-4000-8000-000000000021")),
                any(),
                eq("BOOKING_REACTIVATED:00000000-0000-4000-8000-000000000021:0")
        );
        verify(auditService).record(
                eq(AuditAction.BOOKING_REACTIVATED),
                eq("Booking"),
                eq(UUID.fromString("00000000-0000-4000-8000-000000000021")),
                any(),
                any(),
                any()
        );
    }

    @Test
    @DisplayName("should reject booking after the business-time cutoff")
    void shouldRejectBookingAfterCutoff() {
        Student student = new Student();
        student.setId(UUID.fromString("00000000-0000-4000-8000-000000000007"));
        InterestList interestList = new InterestList();
        interestList.setId(UUID.fromString("00000000-0000-4000-8000-000000000013"));
        interestList.setListStatus(ListStatus.OPEN);
        interestList.setReferenceDate(LocalDate.of(2026, 9, 1));
        interestList.setClosingTime(LocalTime.of(8, 59));
        BookingCreate request = new BookingCreate(
                interestList.getId(), TripType.ROUND_TRIP, null, null);

        given(authentication.getName()).willReturn("12345678900");
        given(studentRepository.findByUser_Cpf("12345678900")).willReturn(Optional.of(student));
        given(interestListRepository.findById(interestList.getId())).willReturn(Optional.of(interestList));

        assertThatThrownBy(() -> bookingService.create(authentication, request))
                .isInstanceOf(IllegalUpdateException.class)
                .hasMessage("This interest list is no longer accepting bookings");

        verify(bookingRepository, never()).save(any());
    }
}
