package com.guilhermesemog.unimove.service;

import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guilhermesemog.unimove.dto.student.StudentResponse;
import com.guilhermesemog.unimove.dto.trip.TripAssignmentUpdate;
import com.guilhermesemog.unimove.dto.trip.TripCreate;
import com.guilhermesemog.unimove.dto.trip.TripPatch;
import com.guilhermesemog.unimove.dto.trip.TripResponse;
import com.guilhermesemog.unimove.exception.type.ResourceAlreadyExists;
import com.guilhermesemog.unimove.exception.type.ResourceNotFoundException;
import com.guilhermesemog.unimove.mapper.StudentMapper;
import com.guilhermesemog.unimove.mapper.TripMapper;
import com.guilhermesemog.unimove.model.Booking;
import com.guilhermesemog.unimove.model.Conductor;
import com.guilhermesemog.unimove.model.InterestList;
import com.guilhermesemog.unimove.model.Student;
import com.guilhermesemog.unimove.model.Trip;
import com.guilhermesemog.unimove.model.TripStudent;
import com.guilhermesemog.unimove.model.Vehicle;
import com.guilhermesemog.unimove.model.enums.ListStatus;
import com.guilhermesemog.unimove.repository.BookingRepository;
import com.guilhermesemog.unimove.repository.ConductorRepository;
import com.guilhermesemog.unimove.repository.InterestListRepository;
import com.guilhermesemog.unimove.repository.StudentRepository;
import com.guilhermesemog.unimove.repository.TripRepository;
import com.guilhermesemog.unimove.repository.TripStudentRepository;
import com.guilhermesemog.unimove.repository.VehicleRepository;

@Service
public class TripService {

    private final TripMapper tripMapper;
    private final StudentMapper studentMapper;

    private final TripRepository tripRepository;
    private final TripStudentRepository tripStudentRepository;
    private final InterestListRepository interestListRepository;
    private final BookingRepository bookingRepository;
    private final StudentRepository studentRepository;
    private final ConductorRepository conductorRepository;
    private final VehicleRepository vehicleRepository;

    public TripService(
            TripMapper tripMapper, StudentMapper studentMapper,
            TripRepository tripRepository, TripStudentRepository tripStudentRepository,
            InterestListRepository interestListRepository, BookingRepository bookingRepository,
            StudentRepository studentRepository, ConductorRepository conductorRepository,
            VehicleRepository vehicleRepository) {
        this.tripMapper = tripMapper;
        this.studentMapper = studentMapper;
        this.tripRepository = tripRepository;
        this.tripStudentRepository = tripStudentRepository;
        this.interestListRepository = interestListRepository;
        this.bookingRepository = bookingRepository;
        this.studentRepository = studentRepository;
        this.conductorRepository = conductorRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional
    public TripResponse create(TripCreate requestBody) {
        if (this.tripRepository.existsByInterestList_Id(requestBody.interestListId())) {
            throw new ResourceAlreadyExists("Trip already exists for this interest list");
        }

        InterestList interestList = getInterestList(requestBody.interestListId());
        List<Booking> bookings = bookingRepository.findAllByInterestList_Id(requestBody.interestListId());
        Trip trip = tripRepository.save(tripMapper.toEntity(requestBody, interestList));

        for (Booking booking : bookings) {
            tripStudentRepository.save(new TripStudent(trip, booking.getStudent()));
        }

        interestList.setListStatus(ListStatus.CLOSED);
        interestListRepository.save(interestList);
        return tripMapper.toResponse(trip);
    }

    public TripResponse getById(Long id) {
        return tripMapper.toResponse(getTrip(id));
    }

    public Page<TripResponse> getAllByUser(Authentication authentication, int page, int size, String sortBy,
            String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return authentication.getAuthorities().stream()
                .anyMatch(authority -> Objects.equals(authority.getAuthority(), "ROLE_STUDENT"))
                        ? getAllTripsByStudent(authentication, pageable)
                        : getAllTripsByConductor(authentication, pageable);

    }

    public Page<TripResponse> getAllTripsByStudent(Authentication authentication, Pageable pageable) {
        Student student = getStudentByAuthentication(authentication);

        return tripStudentRepository.findAllByStudent_Id(student.getId(), pageable)
                .map(TripStudent::getTrip)
                .map(tripMapper::toResponse);
    }

    public Page<TripResponse> getAllTripsByConductor(Authentication authentication, Pageable pageable) {
        Conductor conductor = getConductorByAuthentication(authentication);

        return tripRepository.findAllByConductor_Id(conductor.getId(), pageable)
                .map(tripMapper::toResponse);
    }

    public Page<TripResponse> getAll(int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return tripRepository.findAll(pageable).map(tripMapper::toResponse);
    }

    public Page<StudentResponse> getAllStudentsByTrip(Long id, int page, int size, String sortBy,
            String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<TripStudent> tripStudents = tripStudentRepository.findAllByTrip_Id(id, pageable);

        return tripStudents.map(tripStudent -> {
            Student student = tripStudent.getStudent();
            return studentMapper.toResponse(student);
        });
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

    @Transactional
    public TripResponse updateAssignment(Long id, TripAssignmentUpdate requestBody) {
        Trip trip = getTrip(id);
        Conductor conductor = getConductor(requestBody.conductorId());
        Vehicle vehicle = getVehicle(requestBody.vehicleId());

        trip.setConductor(conductor);
        trip.setVehicle(vehicle);

        return tripMapper.toResponse(tripRepository.save(trip));
    }

    public Page<TripResponse> getAllTripsByConductorOrVehicle(int page, int size, String sortBy, String sortDirection,
            String searchTerm) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return tripRepository.findAllBySearch(searchTerm, pageable).map(tripMapper::toResponse);
    }

    private InterestList getInterestList(Long id) {
        return interestListRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interest list not found"));
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
        return studentRepository.findByUser_Cpf(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    }

    private Conductor getConductorByAuthentication(Authentication authentication) {
        return conductorRepository.findByUser_Cpf(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Conductor not found"));
    }
}
