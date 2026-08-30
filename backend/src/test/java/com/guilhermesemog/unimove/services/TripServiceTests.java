package com.guilhermesemog.unimove.services;

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
import com.guilhermesemog.unimove.model.enums.TripType;
import com.guilhermesemog.unimove.repository.BookingRepository;
import com.guilhermesemog.unimove.repository.ConductorRepository;
import com.guilhermesemog.unimove.repository.InterestListRepository;
import com.guilhermesemog.unimove.repository.StudentRepository;
import com.guilhermesemog.unimove.repository.TripRepository;
import com.guilhermesemog.unimove.repository.TripStudentRepository;
import com.guilhermesemog.unimove.repository.VehicleRepository;
import com.guilhermesemog.unimove.service.TripService;
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
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("TripService Tests")
class TripServiceTests {

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
                vehicleRepository
        );
    }

    @Test
    @DisplayName("should update driver and vehicle as one assignment")
    void shouldUpdateAssignmentAtomically() {
        Trip trip = new Trip();
        Conductor conductor = new Conductor();
        Vehicle vehicle = new Vehicle();

        given(tripRepository.findById(10L)).willReturn(Optional.of(trip));
        given(conductorRepository.findById(20L)).willReturn(Optional.of(conductor));
        given(vehicleRepository.findById(30L)).willReturn(Optional.of(vehicle));
        given(tripRepository.save(trip)).willReturn(trip);
        given(tripMapper.toResponse(trip)).willReturn(tripResponse);

        TripResponse response = service.updateAssignment(10L, new TripAssignmentUpdate(20L, 30L));

        assertThat(trip.getConductor()).isSameAs(conductor);
        assertThat(trip.getVehicle()).isSameAs(vehicle);
        assertThat(response).isSameAs(tripResponse);
        verify(tripRepository).save(trip);
    }

    @Test
    @DisplayName("should return a minimal manifest only for the assigned driver")
    void shouldReturnDriverOperationManifest() {
        Conductor conductor = new Conductor();
        conductor.setId(20L);

        InterestList interestList = new InterestList();
        interestList.setId(40L);
        Trip trip = new Trip(interestList);
        trip.setId(10L);

        User user = new User();
        user.setFirstName("Alex");
        user.setLastName("Morgan");
        user.setPhone("555-0100");
        Student student = new Student();
        student.setUser(user);

        BoardingStop stop = new BoardingStop("Central Station");
        stop.setId(30L);

        Booking booking = new Booking();
        booking.setId(50L);
        booking.setStudent(student);
        booking.setBoardingLocation(stop);
        booking.setTripType(TripType.ROUND_TRIP);
        booking.setBookingStatus(BookingStatus.APPROVED);

        given(authentication.getName()).willReturn("driver-cpf");
        given(conductorRepository.findByUser_Cpf("driver-cpf")).willReturn(Optional.of(conductor));
        given(tripRepository.findByIdAndConductor_Id(10L, 20L)).willReturn(Optional.of(trip));
        given(bookingRepository.findAllByInterestList_IdAndBookingStatus(40L, BookingStatus.APPROVED)).willReturn(List.of(booking));
        given(tripMapper.toResponse(trip)).willReturn(tripResponse);

        DriverOperationResponse response = service.getDriverOperation(authentication, 10L);

        assertThat(response.trip()).isSameAs(tripResponse);
        assertThat(response.passengers()).singleElement().satisfies(passenger -> {
            assertThat(passenger.bookingId()).isEqualTo(50L);
            assertThat(passenger.firstName()).isEqualTo("Alex");
            assertThat(passenger.phone()).isEqualTo("555-0100");
            assertThat(passenger.boardingStop().local()).isEqualTo("Central Station");
            assertThat(passenger.tripType()).isEqualTo(TripType.ROUND_TRIP);
        });
        verify(tripRepository).findByIdAndConductor_Id(10L, 20L);
    }

    @Test
    @DisplayName("should not return an operation assigned to another driver")
    void shouldRejectOperationFromAnotherDriver() {
        Conductor conductor = new Conductor();
        conductor.setId(20L);

        given(authentication.getName()).willReturn("driver-cpf");
        given(conductorRepository.findByUser_Cpf("driver-cpf")).willReturn(Optional.of(conductor));
        given(tripRepository.findByIdAndConductor_Id(10L, 20L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.getDriverOperation(authentication, 10L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Trip not found");

        verify(tripRepository).findByIdAndConductor_Id(10L, 20L);
    }
}
