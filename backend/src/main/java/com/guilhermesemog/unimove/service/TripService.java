package com.guilhermesemog.unimove.service;

import java.util.UUID;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guilhermesemog.unimove.dto.student.StudentResponse;
import com.guilhermesemog.unimove.dto.boardingstop.BoardingStopResponse;
import com.guilhermesemog.unimove.dto.driver.DriverManifestPassengerResponse;
import com.guilhermesemog.unimove.dto.driver.DriverOperationResponse;
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
import com.guilhermesemog.unimove.model.enums.AuditAction;
import com.guilhermesemog.unimove.model.enums.BookingStatus;
import com.guilhermesemog.unimove.model.enums.BusinessEventType;
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
    private final BusinessEventPublisher eventPublisher;
    private final AuditService auditService;
    private final Clock businessClock;

    public TripService(
            TripMapper tripMapper, StudentMapper studentMapper,
            TripRepository tripRepository, TripStudentRepository tripStudentRepository,
            InterestListRepository interestListRepository, BookingRepository bookingRepository,
            StudentRepository studentRepository, ConductorRepository conductorRepository,
            VehicleRepository vehicleRepository, BusinessEventPublisher eventPublisher,
            AuditService auditService, Clock businessClock) {
        this.tripMapper = tripMapper;
        this.studentMapper = studentMapper;
        this.tripRepository = tripRepository;
        this.tripStudentRepository = tripStudentRepository;
        this.interestListRepository = interestListRepository;
        this.bookingRepository = bookingRepository;
        this.studentRepository = studentRepository;
        this.conductorRepository = conductorRepository;
        this.vehicleRepository = vehicleRepository;
        this.eventPublisher = eventPublisher;
        this.auditService = auditService;
        this.businessClock = businessClock;
    }

    @Transactional
    public TripResponse create(TripCreate requestBody) {
        if (this.tripRepository.existsByInterestList_Id(requestBody.interestListId())) {
            throw new ResourceAlreadyExists("Trip already exists for this interest list");
        }

        InterestList interestList = getInterestList(requestBody.interestListId());
        List<Booking> bookings = bookingRepository.findAllByInterestList_IdAndBookingStatusNot(
                requestBody.interestListId(), BookingStatus.CANCELLED);
        Trip trip = tripRepository.save(tripMapper.toEntity(requestBody, interestList));

        for (Booking booking : bookings) {
            tripStudentRepository.save(new TripStudent(trip, booking.getStudent()));
        }

        interestList.setListStatus(ListStatus.CLOSED);
        interestList.setStatusChangedAt(businessClock.instant());
        interestListRepository.save(interestList);
        eventPublisher.publish(
                BusinessEventType.TRIP_CREATED,
                "Trip",
                trip.getId(),
                Map.of(
                        "tripId", trip.getId(),
                        "interestListId", interestList.getId(),
                        "passengerCount", bookings.size()
                ),
                BusinessEventType.TRIP_CREATED.name() + ":" + trip.getId()
        );
        auditService.record(
                AuditAction.TRIP_CREATED,
                "Trip",
                trip.getId(),
                Map.of(),
                tripAuditState(trip),
                Map.of("passengerCount", bookings.size())
        );
        return tripMapper.toResponse(trip);
    }

    public TripResponse getById(UUID id) {
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

    @Transactional(readOnly = true)
    public DriverOperationResponse getDriverOperation(Authentication authentication, UUID tripId) {
        Conductor conductor = getConductorByAuthentication(authentication);
        Trip trip = tripRepository.findByIdAndConductor_Id(tripId, conductor.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        List<DriverManifestPassengerResponse> passengers = bookingRepository
                .findAllByInterestList_IdAndBookingStatus(trip.getInterestList().getId(), BookingStatus.APPROVED)
                .stream()
                .map(booking -> new DriverManifestPassengerResponse(
                        booking.getId(),
                        booking.getStudent().getUser().getFirstName(),
                        booking.getStudent().getUser().getLastName(),
                        booking.getStudent().getUser().getPhone(),
                        new BoardingStopResponse(
                                booking.getBoardingLocation().getId(),
                                booking.getBoardingLocation().getLocal()
                        ),
                        booking.getTripType()
                ))
                .sorted(java.util.Comparator
                        .comparing((DriverManifestPassengerResponse passenger) -> passenger.boardingStop().local(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(DriverManifestPassengerResponse::firstName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(DriverManifestPassengerResponse::lastName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        return new DriverOperationResponse(tripMapper.toResponse(trip), passengers);
    }

    public Page<TripResponse> getAll(int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return tripRepository.findAll(pageable).map(tripMapper::toResponse);
    }

    public Page<StudentResponse> getAllStudentsByTrip(UUID id, int page, int size, String sortBy,
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

    @Transactional
    public void updateConductor(UUID id, TripPatch requestBody) {
        Trip trip = getTrip(id);
        Map<String, Object> previousState = assignmentAuditState(trip);
        Conductor conductor = getConductor(requestBody.conductorId());
        trip.setConductor(conductor);
        updateAssignmentTimestamp(trip);
        Trip savedTrip = tripRepository.save(trip);
        publishAssignmentChange(savedTrip, previousState);
        auditAssignmentChange(savedTrip, previousState);
    }

    @Transactional
    public void updateVehicle(UUID id, TripPatch requestBody) {
        Trip trip = getTrip(id);
        Map<String, Object> previousState = assignmentAuditState(trip);
        Vehicle vehicle = getVehicle(requestBody.vehicleId());
        trip.setVehicle(vehicle);
        updateAssignmentTimestamp(trip);
        Trip savedTrip = tripRepository.save(trip);
        publishAssignmentChange(savedTrip, previousState);
        auditAssignmentChange(savedTrip, previousState);
    }

    @Transactional
    public TripResponse updateAssignment(UUID id, TripAssignmentUpdate requestBody) {
        Trip trip = getTrip(id);
        Map<String, Object> previousState = assignmentAuditState(trip);
        Conductor conductor = getConductor(requestBody.conductorId());
        Vehicle vehicle = getVehicle(requestBody.vehicleId());

        trip.setConductor(conductor);
        trip.setVehicle(vehicle);
        updateAssignmentTimestamp(trip);

        Trip savedTrip = tripRepository.save(trip);
        publishAssignmentChange(savedTrip, previousState);
        auditAssignmentChange(savedTrip, previousState);
        return tripMapper.toResponse(savedTrip);
    }

    public Page<TripResponse> getAllTripsByConductorOrVehicle(int page, int size, String sortBy, String sortDirection,
            String searchTerm) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return tripRepository.findAllBySearch(searchTerm, pageable).map(tripMapper::toResponse);
    }

    private InterestList getInterestList(UUID id) {
        return interestListRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interest list not found"));
    }

    private Trip getTrip(UUID id) {
        return tripRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Trip not found"));
    }

    private Vehicle getVehicle(UUID id) {
        return vehicleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
    }

    private Student getStudent(UUID id) {
        return studentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    }

    private Conductor getConductor(UUID id) {
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

    private void updateAssignmentTimestamp(Trip trip) {
        if (trip.getConductor() != null && trip.getVehicle() != null && trip.getAssignedAt() == null) {
            trip.setAssignedAt(businessClock.instant());
        }
    }

    private void publishAssignmentChange(Trip trip, Map<String, Object> previousState) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("tripId", trip.getId());
        payload.put("previousConductorId", previousState.get("conductorId"));
        payload.put("previousVehicleId", previousState.get("vehicleId"));
        payload.put("conductorId", trip.getConductor() == null ? null : trip.getConductor().getId());
        payload.put("vehicleId", trip.getVehicle() == null ? null : trip.getVehicle().getId());
        payload.put("assignedAt", trip.getAssignedAt() == null ? null : trip.getAssignedAt().toString());
        eventPublisher.publish(
                BusinessEventType.TRIP_ASSIGNMENT_CHANGED,
                "Trip",
                trip.getId(),
                payload,
                BusinessEventType.TRIP_ASSIGNMENT_CHANGED.name() + ":" + trip.getId() + ":" + trip.getVersion()
        );
    }

    private void auditAssignmentChange(Trip trip, Map<String, Object> previousState) {
        auditService.record(
                AuditAction.TRIP_ASSIGNMENT_CHANGED,
                "Trip",
                trip.getId(),
                previousState,
                assignmentAuditState(trip),
                Map.of()
        );
    }

    private Map<String, Object> tripAuditState(Trip trip) {
        Map<String, Object> state = assignmentAuditState(trip);
        state.put("interestListId", trip.getInterestList().getId());
        state.put("status", trip.getStatus().name());
        return state;
    }

    private Map<String, Object> assignmentAuditState(Trip trip) {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("conductorId", trip.getConductor() == null ? null : trip.getConductor().getId());
        state.put("vehicleId", trip.getVehicle() == null ? null : trip.getVehicle().getId());
        state.put("assignedAt", trip.getAssignedAt() == null ? null : trip.getAssignedAt().toString());
        return state;
    }
}
