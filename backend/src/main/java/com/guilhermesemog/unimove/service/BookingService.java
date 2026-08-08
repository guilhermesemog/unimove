package com.guilhermesemog.unimove.service;

import com.guilhermesemog.unimove.dto.booking.BookingCreate;
import com.guilhermesemog.unimove.dto.booking.BookingResponse;
import com.guilhermesemog.unimove.exception.type.IllegalUpdateException;
import com.guilhermesemog.unimove.exception.type.ResourceAlreadyExists;
import com.guilhermesemog.unimove.exception.type.ResourceNotFoundException;
import com.guilhermesemog.unimove.mapper.BookingMapper;
import com.guilhermesemog.unimove.model.*;
import com.guilhermesemog.unimove.model.enums.BookingStatus;
import com.guilhermesemog.unimove.model.enums.ListStatus;
import com.guilhermesemog.unimove.model.enums.TripType;
import com.guilhermesemog.unimove.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingService {

    private final BookingMapper bookingMapper;

    private final BookingRepository bookingRepository;
    private final StudentRepository studentRepository;
    private final BoardingStopRepository boardingStopRepository;
    private final InterestListRepository interestListRepository;
    private final UniversityRepository universityRepository;

    public BookingService(
            BookingMapper bookingMapper, BookingRepository bookingRepository,
            StudentRepository studentRepository, BoardingStopRepository boardingStopRepository,
            InterestListRepository interestListRepository, UniversityRepository universityRepository
    ) {
        this.bookingMapper = bookingMapper;
        this.bookingRepository = bookingRepository;
        this.studentRepository = studentRepository;
        this.boardingStopRepository = boardingStopRepository;
        this.interestListRepository = interestListRepository;
        this.universityRepository = universityRepository;
    }

    public void create(Authentication authentication, BookingCreate requestBody) {
        Student student = getStudentByAuthentication(authentication);

        InterestList interestList = interestListRepository.findById(requestBody.interestListId())
                .orElseThrow(() -> new ResourceNotFoundException("Interest list not found"));

        if (bookingRepository.existsByStudent_IdAndInterestList_Id(student.getId(), requestBody.interestListId())) {
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

        Booking booking = bookingMapper.toEntity(student, interestList, bookingStatus, tripType, university, boardingStop);

        bookingRepository.save(booking);
    }

    public BookingResponse getById(Long id) {
        return bookingRepository.findById(id)
                .map(booking -> bookingMapper.toResponse(booking, booking.getStudent(), booking.getInterestList(), booking.getDestination(), booking.getBoardingLocation()))
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
    }

    public Page<BookingResponse> getAllBookingsByInterestList(Long interestListId, int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return bookingRepository.findAllByInterestList_Id(interestListId, pageable)
                .map(booking -> bookingMapper.toResponse(booking, booking.getStudent(), booking.getInterestList(), booking.getDestination(), booking.getBoardingLocation()));
    }

    public Page<BookingResponse> getAllBookingsByTrip(Long tripId, int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return bookingRepository.findAllByInterestList_Id(tripId, pageable)
                .map(booking -> bookingMapper.toResponse(booking, booking.getStudent(), booking.getInterestList(), booking.getDestination(), booking.getBoardingLocation()));
    }

    public List<BookingResponse> getAllBookingsByInterestList(Long interestListId) {
        return bookingRepository.findAllByInterestList_Id(interestListId)
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

        return bookingRepository.findAllByStudentId(student.getId(), pageable)
                .map(booking -> bookingMapper.toResponse(booking, booking.getStudent(), booking.getInterestList(), booking.getDestination(), booking.getBoardingLocation()));
    }

    public void deleteById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (booking.getInterestList().getListStatus() != ListStatus.OPEN) {
            throw new IllegalUpdateException("Cannot delete booking for a closed interest list");
        }

        bookingRepository.delete(booking);
    }

    private Student getStudentByAuthentication(Authentication authentication) {
        return studentRepository.findByUser_Cpf(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    }

    private InterestList getInterestList(Long id) {
        return interestListRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interest list not found"));
    }
}