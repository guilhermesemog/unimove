package com.guilhermesemog.unimove.services;

import java.util.UUID;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import com.guilhermesemog.unimove.dto.trip.TripAssignmentUpdate;
import com.guilhermesemog.unimove.dto.trip.TripResponse;
import com.guilhermesemog.unimove.dto.driver.DriverOperationResponse;
import com.guilhermesemog.unimove.exception.type.ResourceNotFoundException;
import com.guilhermesemog.unimove.mapper.StudentMapper;
import com.guilhermesemog.unimove.mapper.TripMapper;
import com.guilhermesemog.unimove.model.Conductor;
import com.guilhermesemog.unimove.model.Booking;
import com.guilhermesemog.unimove.model.BoardingStop;
import com.guilhermesemog.unimove.model.InterestList;
import com.guilhermesemog.unimove.model.Student;
import com.guilhermesemog.unimove.model.Trip;
import com.guilhermesemog.unimove.model.User;
import com.guilhermesemog.unimove.model.Vehicle;
import com.guilhermesemog.unimove.model.enums.BookingStatus;
import com.guilhermesemog.unimove.model.enums.AuditAction;
import com.guilhermesemog.unimove.model.enums.TripType;
import com.guilhermesemog.unimove.repository.BookingRepository;
import com.guilhermesemog.unimove.repository.ConductorRepository;
import com.guilhermesemog.unimove.repository.InterestListRepository;
import com.guilhermesemog.unimove.repository.StudentRepository;
import com.guilhermesemog.unimove.repository.TripRepository;
import com.guilhermesemog.unimove.repository.TripStudentRepository;
import com.guilhermesemog.unimove.repository.VehicleRepository;
import com.guilhermesemog.unimove.service.TripService;
import com.guilhermesemog.unimove.service.BusinessEventPublisher;
import com.guilhermesemog.unimove.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("TripService Tests")
class TripServiceTests {

    private static final Instant NOW = Instant.parse("2026-09-01T12:00:00Z");
    private static final Clock BUSINESS_CLOCK = Clock.fixed(NOW, ZoneId.of("America/Sao_Paulo"));

    @Mock private TripMapper tripMapper;
    @Mock private StudentMapper studentMapper;
    @Mock private TripRepository tripRepository;
    @Mock private TripStudentRepository tripStudentRepository;
    @Mock private InterestListRepository interestListRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private ConductorRepository conductorRepository;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private TripResponse tripResponse;
    @Mock private Authentication authentication;
    @Mock private BusinessEventPublisher eventPublisher;
    @Mock private AuditService auditService;

    private TripService service;

    @BeforeEach
    void setUp() {
        service = new TripService(
                tripMapper,
                studentMapper,
                tripRepository,
                tripStudentRepository,
                interestListRepository,
                bookingRepository,
                studentRepository,
                conductorRepository,
                vehicleRepository,
                eventPublisher,
                auditService,
                BUSINESS_CLOCK
        );
    }

    @Test
    @DisplayName("should update driver and vehicle as one assignment")
    void shouldUpdateAssignmentAtomically() {
        Trip trip = new Trip();
        trip.setId(UUID.fromString("00000000-0000-4000-8000-000000000010"));
        Conductor conductor = new Conductor();
        conductor.setId(UUID.fromString("00000000-0000-4000-8000-000000000020"));
        Vehicle vehicle = new Vehicle();
        vehicle.setId(UUID.fromString("00000000-0000-4000-8000-000000000030"));

        given(tripRepository.findById(UUID.fromString("00000000-0000-4000-8000-000000000010"))).willReturn(Optional.of(trip));
        given(conductorRepository.findById(UUID.fromString("00000000-0000-4000-8000-000000000020"))).willReturn(Optional.of(conductor));
        given(vehicleRepository.findById(UUID.fromString("00000000-0000-4000-8000-000000000030"))).willReturn(Optional.of(vehicle));
        given(tripRepository.save(trip)).willReturn(trip);
        given(tripMapper.toResponse(trip)).willReturn(tripResponse);

        TripResponse response = service.updateAssignment(UUID.fromString("00000000-0000-4000-8000-000000000010"), new TripAssignmentUpdate(UUID.fromString("00000000-0000-4000-8000-000000000020"), UUID.fromString("00000000-0000-4000-8000-000000000030")));

        assertThat(trip.getConductor()).isSameAs(conductor);
        assertThat(trip.getVehicle()).isSameAs(vehicle);
        assertThat(trip.getAssignedAt()).isNotNull();
        assertThat(trip.getAssignedAt()).isEqualTo(NOW);
        assertThat(response).isSameAs(tripResponse);
        verify(tripRepository).save(trip);
        verify(auditService).record(
                eq(AuditAction.TRIP_ASSIGNMENT_CHANGED),
                eq("Trip"),
                eq(UUID.fromString("00000000-0000-4000-8000-000000000010")),
                any(),
                any(),
                any()
        );
    }

    @Test
    @DisplayName("should preserve the first complete assignment timestamp")
    void shouldPreserveFirstCompleteAssignmentTimestamp() {
        Instant firstAssignment = Instant.parse("2026-08-30T12:00:00Z");
        Trip trip = new Trip();
        trip.setId(UUID.fromString("00000000-0000-4000-8000-000000000010"));
        trip.setAssignedAt(firstAssignment);
        Conductor conductor = new Conductor();
        conductor.setId(UUID.fromString("00000000-0000-4000-8000-000000000020"));
        Vehicle vehicle = new Vehicle();
        vehicle.setId(UUID.fromString("00000000-0000-4000-8000-000000000030"));

        given(tripRepository.findById(trip.getId())).willReturn(Optional.of(trip));
        given(conductorRepository.findById(conductor.getId())).willReturn(Optional.of(conductor));
        given(vehicleRepository.findById(vehicle.getId())).willReturn(Optional.of(vehicle));
        given(tripRepository.save(trip)).willReturn(trip);
        given(tripMapper.toResponse(trip)).willReturn(tripResponse);

        service.updateAssignment(trip.getId(), new TripAssignmentUpdate(conductor.getId(), vehicle.getId()));

        assertThat(trip.getAssignedAt()).isEqualTo(firstAssignment);
    }

    @Test
    @DisplayName("should return a minimal manifest only for the assigned driver")
    void shouldReturnDriverOperationManifest() {
        Conductor conductor = new Conductor();
        conductor.setId(UUID.fromString("00000000-0000-4000-8000-000000000020"));

        InterestList interestList = new InterestList();
        interestList.setId(UUID.fromString("00000000-0000-4000-8000-000000000040"));
        Trip trip = new Trip(interestList);
        trip.setId(UUID.fromString("00000000-0000-4000-8000-000000000010"));

        User user = new User();
        user.setFirstName("Alex");
        user.setLastName("Morgan");
        user.setPhone("555-0100");
        Student student = new Student();
        student.setUser(user);

        BoardingStop stop = new BoardingStop("Central Station");
        stop.setId(UUID.fromString("00000000-0000-4000-8000-000000000030"));

        Booking booking = new Booking();
        booking.setId(UUID.fromString("00000000-0000-4000-8000-000000000050"));
        booking.setStudent(student);
        booking.setBoardingLocation(stop);
        booking.setTripType(TripType.ROUND_TRIP);
        booking.setBookingStatus(BookingStatus.APPROVED);

        given(authentication.getName()).willReturn("driver-cpf");
        given(conductorRepository.findByUser_Cpf("driver-cpf")).willReturn(Optional.of(conductor));
        given(tripRepository.findByIdAndConductor_Id(UUID.fromString("00000000-0000-4000-8000-000000000010"), UUID.fromString("00000000-0000-4000-8000-000000000020"))).willReturn(Optional.of(trip));
        given(bookingRepository.findAllByInterestList_IdAndBookingStatus(UUID.fromString("00000000-0000-4000-8000-000000000040"), BookingStatus.APPROVED)).willReturn(List.of(booking));
        given(tripMapper.toResponse(trip)).willReturn(tripResponse);

        DriverOperationResponse response = service.getDriverOperation(authentication, UUID.fromString("00000000-0000-4000-8000-000000000010"));

        assertThat(response.trip()).isSameAs(tripResponse);
        assertThat(response.passengers()).singleElement().satisfies(passenger -> {
            assertThat(passenger.bookingId()).isEqualTo(UUID.fromString("00000000-0000-4000-8000-000000000050"));
            assertThat(passenger.firstName()).isEqualTo("Alex");
            assertThat(passenger.phone()).isEqualTo("555-0100");
            assertThat(passenger.boardingStop().local()).isEqualTo("Central Station");
            assertThat(passenger.tripType()).isEqualTo(TripType.ROUND_TRIP);
        });
        verify(tripRepository).findByIdAndConductor_Id(UUID.fromString("00000000-0000-4000-8000-000000000010"), UUID.fromString("00000000-0000-4000-8000-000000000020"));
    }

    @Test
    @DisplayName("should not return an operation assigned to another driver")
    void shouldRejectOperationFromAnotherDriver() {
        Conductor conductor = new Conductor();
        conductor.setId(UUID.fromString("00000000-0000-4000-8000-000000000020"));

        given(authentication.getName()).willReturn("driver-cpf");
        given(conductorRepository.findByUser_Cpf("driver-cpf")).willReturn(Optional.of(conductor));
        given(tripRepository.findByIdAndConductor_Id(UUID.fromString("00000000-0000-4000-8000-000000000010"), UUID.fromString("00000000-0000-4000-8000-000000000020"))).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.getDriverOperation(authentication, UUID.fromString("00000000-0000-4000-8000-000000000010")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Trip not found");

        verify(tripRepository).findByIdAndConductor_Id(UUID.fromString("00000000-0000-4000-8000-000000000010"), UUID.fromString("00000000-0000-4000-8000-000000000020"));
    }
}
