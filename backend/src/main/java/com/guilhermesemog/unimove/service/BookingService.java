package com.guilhermesemog.unimove.service;

import java.util.UUID;
import com.guilhermesemog.unimove.dto.booking.BookingCreate;
import com.guilhermesemog.unimove.dto.booking.BookingResponse;
import com.guilhermesemog.unimove.exception.type.IllegalUpdateException;
import com.guilhermesemog.unimove.exception.type.ResourceAlreadyExists;
import com.guilhermesemog.unimove.exception.type.ResourceNotFoundException;
import com.guilhermesemog.unimove.mapper.BookingMapper;
import com.guilhermesemog.unimove.model.*;
import com.guilhermesemog.unimove.model.enums.AuditAction;
import com.guilhermesemog.unimove.model.enums.BookingStatus;
import com.guilhermesemog.unimove.model.enums.BusinessEventType;
import com.guilhermesemog.unimove.model.enums.ListStatus;
import com.guilhermesemog.unimove.model.enums.TripType;
import com.guilhermesemog.unimove.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BookingService {

    private final BookingMapper bookingMapper;

    private final BookingRepository bookingRepository;
    private final StudentRepository studentRepository;
    private final BoardingStopRepository boardingStopRepository;
    private final InterestListRepository interestListRepository;
    private final UniversityRepository universityRepository;
    private final BusinessEventPublisher eventPublisher;
    private final AuditService auditService;
    private final Clock businessClock;

    public BookingService(
            BookingMapper bookingMapper, BookingRepository bookingRepository,
            StudentRepository studentRepository, BoardingStopRepository boardingStopRepository,
            InterestListRepository interestListRepository, UniversityRepository universityRepository,
            BusinessEventPublisher eventPublisher, AuditService auditService, Clock businessClock
    ) {
        this.bookingMapper = bookingMapper;
        this.bookingRepository = bookingRepository;
        this.studentRepository = studentRepository;
        this.boardingStopRepository = boardingStopRepository;
        this.interestListRepository = interestListRepository;
        this.universityRepository = universityRepository;
        this.eventPublisher = eventPublisher;
        this.auditService = auditService;
        this.businessClock = businessClock;
    }

    @Transactional
    public void create(Authentication authentication, BookingCreate requestBody) {
        Student student = getStudentByAuthentication(authentication);

        InterestList interestList = interestListRepository.findById(requestBody.interestListId())
                .orElseThrow(() -> new ResourceNotFoundException("Interest list not found"));

        if (!isBookingOpen(interestList)) {
            throw new IllegalUpdateException("This interest list is no longer accepting bookings");
        }

        Booking existingBooking = bookingRepository
                .findByStudent_IdAndInterestList_Id(student.getId(), requestBody.interestListId())
                .orElse(null);

        if (existingBooking != null && existingBooking.getBookingStatus() != BookingStatus.CANCELLED) {
            throw new ResourceAlreadyExists("Student already has a booking");
        }

        if (requestBody.boardingStopId() == null && student.getPreferredBoardingStop() == null) {
            throw new ResourceNotFoundException("No preferred boarding stop found for the student, the boarding stop must be specified");
        }

        BoardingStop boardingStop = requestBody.boardingStopId() == null ? student.getPreferredBoardingStop() : boardingStopRepository.findById(requestBody.boardingStopId())
                .orElseThrow(() -> new ResourceNotFoundException("Boarding stop not found"));

        University university = requestBody.universityId() == null ? student.getUniversity() : universityRepository.findById(requestBody.universityId())
                .orElseThrow(() -> new ResourceNotFoundException("University not found"));

        BookingStatus bookingStatus = BookingStatus.APPROVED;
        TripType tripType = requestBody.tripType();

        Booking booking;
        BusinessEventType eventType;
        AuditAction auditAction;
        Map<String, Object> previousState = existingBooking == null
                ? Map.of()
                : bookingAuditState(existingBooking);

        if (existingBooking == null) {
            booking = bookingMapper.toEntity(student, interestList, bookingStatus, tripType, university, boardingStop);
            eventType = BusinessEventType.BOOKING_CREATED;
            auditAction = AuditAction.BOOKING_CREATED;
        } else {
            booking = existingBooking;
            booking.setBookingStatus(BookingStatus.APPROVED);
            booking.setTripType(tripType);
            booking.setDestination(university);
            booking.setBoardingLocation(boardingStop);
            booking.setCancelledAt(null);
            eventType = BusinessEventType.BOOKING_REACTIVATED;
            auditAction = AuditAction.BOOKING_REACTIVATED;
        }

        Booking savedBooking = bookingRepository.save(booking);
        eventPublisher.publish(
                eventType,
                "Booking",
                savedBooking.getId(),
                Map.of(
                        "bookingId", savedBooking.getId(),
                        "studentId", student.getId(),
                        "interestListId", interestList.getId(),
                        "tripType", tripType.name()
                ),
                eventType.name() + ":" + savedBooking.getId() + ":" + savedBooking.getVersion()
        );
        auditService.record(
                auditAction,
                "Booking",
                savedBooking.getId(),
                previousState,
                bookingAuditState(savedBooking),
                Map.of()
        );
    }

    public BookingResponse getById(UUID id) {
        return bookingRepository.findById(id)
                .map(booking -> bookingMapper.toResponse(booking, booking.getStudent(), booking.getInterestList(), booking.getDestination(), booking.getBoardingLocation()))
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
    }

    public Page<BookingResponse> getAllBookingsByInterestList(UUID interestListId, int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return bookingRepository.findAllByInterestList_IdAndBookingStatusNot(interestListId, BookingStatus.CANCELLED, pageable)
                .map(booking -> bookingMapper.toResponse(booking, booking.getStudent(), booking.getInterestList(), booking.getDestination(), booking.getBoardingLocation()));
    }

    public Page<BookingResponse> getAllBookingsByTrip(UUID tripId, int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return bookingRepository.findAllByInterestList_IdAndBookingStatusNot(tripId, BookingStatus.CANCELLED, pageable)
                .map(booking -> bookingMapper.toResponse(booking, booking.getStudent(), booking.getInterestList(), booking.getDestination(), booking.getBoardingLocation()));
    }

    public List<BookingResponse> getAllBookingsByInterestList(UUID interestListId) {
        return bookingRepository.findAllByInterestList_IdAndBookingStatusNot(interestListId, BookingStatus.CANCELLED)
                .stream()
                .map(booking -> bookingMapper.toResponse(booking, booking.getStudent(), booking.getInterestList(), booking.getDestination(), booking.getBoardingLocation()))
                .toList();
    }

    public Page<BookingResponse> getAllBookingsByStudent(Authentication authentication, int page, int size, String sortBy, String sortDirection) {
        Student student = getStudentByAuthentication(authentication);

        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return bookingRepository.findAllByStudentIdAndBookingStatusNot(student.getId(), BookingStatus.CANCELLED, pageable)
                .map(booking -> bookingMapper.toResponse(booking, booking.getStudent(), booking.getInterestList(), booking.getDestination(), booking.getBoardingLocation()));
    }

    @Transactional
    public void deleteById(UUID id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!isBookingOpen(booking.getInterestList())) {
            throw new IllegalUpdateException("Cannot cancel a booking after the interest list closes");
        }

        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new IllegalUpdateException("Booking is already cancelled");
        }

        Map<String, Object> previousState = bookingAuditState(booking);
        booking.setBookingStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(businessClock.instant());
        Booking cancelledBooking = bookingRepository.saveAndFlush(booking);
        eventPublisher.publish(
                BusinessEventType.BOOKING_CANCELLED,
                "Booking",
                cancelledBooking.getId(),
                Map.of(
                        "bookingId", cancelledBooking.getId(),
                        "studentId", cancelledBooking.getStudent().getId(),
                        "interestListId", cancelledBooking.getInterestList().getId()
                ),
                BusinessEventType.BOOKING_CANCELLED.name() + ":" + cancelledBooking.getId() + ":" + cancelledBooking.getVersion()
        );
        auditService.record(
                AuditAction.BOOKING_CANCELLED,
                "Booking",
                cancelledBooking.getId(),
                previousState,
                bookingAuditState(cancelledBooking),
                Map.of()
        );
    }

    private Student getStudentByAuthentication(Authentication authentication) {
        return studentRepository.findByUser_Cpf(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    }

    private InterestList getInterestList(UUID id) {
        return interestListRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interest list not found"));
    }

    private boolean isBookingOpen(InterestList interestList) {
        if (interestList.getListStatus() != ListStatus.OPEN || interestList.getClosingTime() == null) {
            return false;
        }

        LocalDateTime closesAt = LocalDateTime.of(interestList.getReferenceDate(), interestList.getClosingTime());
        return LocalDateTime.now(businessClock).isBefore(closesAt);
    }

    private Map<String, Object> bookingAuditState(Booking booking) {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("status", booking.getBookingStatus().name());
        state.put("studentId", booking.getStudent().getId());
        state.put("interestListId", booking.getInterestList().getId());
        state.put("tripType", booking.getTripType() == null ? null : booking.getTripType().name());
        state.put("destinationId", booking.getDestination() == null ? null : booking.getDestination().getId());
        state.put("boardingStopId", booking.getBoardingLocation() == null ? null : booking.getBoardingLocation().getId());
        state.put("cancelledAt", booking.getCancelledAt() == null ? null : booking.getCancelledAt().toString());
        return state;
    }
}
