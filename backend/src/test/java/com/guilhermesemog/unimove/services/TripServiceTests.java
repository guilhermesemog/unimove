package com.guilhermesemog.unimove.services;

import com.guilhermesemog.unimove.dto.trip.TripAssignmentUpdate;
import com.guilhermesemog.unimove.dto.trip.TripResponse;
import com.guilhermesemog.unimove.mapper.StudentMapper;
import com.guilhermesemog.unimove.mapper.TripMapper;
import com.guilhermesemog.unimove.model.Conductor;
import com.guilhermesemog.unimove.model.Trip;
import com.guilhermesemog.unimove.model.Vehicle;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
}
