package com.guilhermesemog.unimove.service;

import com.guilhermesemog.unimove.dto.trip.TripCreate;
import com.guilhermesemog.unimove.dto.trip.TripPatch;
import com.guilhermesemog.unimove.dto.trip.TripResponse;
import com.guilhermesemog.unimove.exception.type.ResourceNotFoundException;
import com.guilhermesemog.unimove.mapper.TripMapper;
import com.guilhermesemog.unimove.model.*;
import com.guilhermesemog.unimove.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class TripService {

    private final TripMapper tripMapper;
    private final TripRepository tripRepository;
    private final TripStudentRepository tripStudentRepository;
    private final InterestListRepository interestListRepository;
    private final BookingRepository bookingRepository;
    private final StudentRepository studentRepository;
    private final ConductorRepository conductorRepository;
    private final VehicleRepository vehicleRepository;

    public TripService(TripMapper tripMapper, TripRepository tripRepository, TripStudentRepository tripStudentRepository, InterestListRepository interestListRepository, BookingRepository bookingRepository, StudentRepository studentRepository, ConductorRepository conductorRepository, VehicleRepository vehicleRepository) {
        this.tripMapper = tripMapper;
        this.tripRepository = tripRepository;
        this.tripStudentRepository = tripStudentRepository;
        this.interestListRepository = interestListRepository;
        this.bookingRepository = bookingRepository;
        this.studentRepository = studentRepository;
        this.conductorRepository = conductorRepository;
        this.vehicleRepository = vehicleRepository;
    }

    public TripResponse create(TripCreate requestBody) {
        InterestList interestList = getInterestList(requestBody.interestListId());

        List<Booking> bookings = bookingRepository.findByInterestList_Id(requestBody.interestListId());

        Trip trip = tripRepository.save(tripMapper.toEntity(requestBody, interestList));

        for (Booking booking : bookings) {
            tripStudentRepository.save(new TripStudent(trip, booking.getStudent()));
        }

        return tripMapper.toResponse(trip);
    }

    public TripResponse getById(Long id) {
        return tripMapper.toResponse(getTrip(id));
    }

    public List<TripResponse> getAllByUser(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> Objects.equals(authority.getAuthority(), "ROLE_STUDENT"))
                ? getAllTripsByStudent(authentication)
                : getAllTripsByConductor(authentication);

    }

    public List<TripResponse> getAllTripsByStudent(Authentication authentication) {
        Student student = getStudentByAuthentication(authentication);

        return tripStudentRepository.findAllByStudent_Id(student.getId())
                .stream()
                .map(TripStudent::getTrip)
                .map(tripMapper::toResponse)
                .toList();
    }

    public List<TripResponse> getAllTripsByConductor(Authentication authentication) {
        Conductor conductor = getConductorByAuthentication(authentication);

        return tripRepository.findAllByConductor_Id(conductor.getId())
                .stream()
                .map(tripMapper::toResponse)
                .toList();
    }

    public Page<TripResponse> getAll(int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return tripRepository.findAll(pageable).map(tripMapper::toResponse);
    }

    public void updateConductor(Long id, TripPatch requestBody) {
        Trip trip = getTrip(id);
        Conductor conductor = getConductor(requestBody.conductorId());
        trip.setConductor(conductor);
        tripRepository.save(trip);
    }

    public void updateVehicle(Long id, TripPatch requestBody) {
        Trip trip = getTrip(id);
        Vehicle vehicle = getVehicle(requestBody.vehicleId());
        trip.setVehicle(vehicle);
        tripRepository.save(trip);
    }

    public Page<TripResponse> getAllTripsByConductorOrVehicle(int page, int size, String sortBy, String sortDirection, String searchTerm) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return tripRepository.findAllBySearch(searchTerm, pageable).map(tripMapper::toResponse);
    }

    private InterestList getInterestList(Long id) {
        return interestListRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Interest list not found"));
    }

    private Trip getTrip(Long id) {
        return tripRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Trip not found"));
    }

    private Vehicle getVehicle(Long id) {
        return vehicleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
    }

    private Student getStudent(Long id) {
        return studentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    }

    private Conductor getConductor(Long id) {
        return conductorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Conductor not found"));
    }

    private Student getStudentByAuthentication(Authentication authentication) {
        return studentRepository.findByUser_Cpf(authentication.getName()).orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    }

    private Conductor getConductorByAuthentication(Authentication authentication) {
        return conductorRepository.findByUser_Cpf(authentication.getName()).orElseThrow(() -> new ResourceNotFoundException("Conductor not found"));
    }
}
