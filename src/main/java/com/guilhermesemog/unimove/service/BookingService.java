package com.guilhermesemog.unimove.service;

import com.guilhermesemog.unimove.dto.booking.BookingCreate;
import com.guilhermesemog.unimove.dto.booking.BookingResponse;
import com.guilhermesemog.unimove.exception.BookingAlreadyExists;
import com.guilhermesemog.unimove.exception.ResourceNotFoundException;
import com.guilhermesemog.unimove.mapper.BookingMapper;
import com.guilhermesemog.unimove.model.*;
import com.guilhermesemog.unimove.model.enums.BookingStatus;
import com.guilhermesemog.unimove.model.enums.ListStatus;
import com.guilhermesemog.unimove.model.enums.TripType;
import com.guilhermesemog.unimove.repository.*;
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

    public BookingService(BookingMapper bookingMapper, BookingRepository bookingRepository, StudentRepository studentRepository, BoardingStopRepository boardingStopRepository, InterestListRepository interestListRepository, UniversityRepository universityRepository) {
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
            throw new BookingAlreadyExists("Student already has a booking");
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
        return bookingRepository.findById(id).map(bookingMapper::toResponse).orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
    }

    public List<BookingResponse> getAllBookingsByInterestList(Long interestListId) {
        return bookingRepository.findByInterestList_Id(interestListId)
                .stream().map(bookingMapper::toResponse).toList();
    }

    public List<BookingResponse> getAllBookingsByStudent(Authentication authentication) {
        Student student = getStudentByAuthentication(authentication);

        return bookingRepository.findByStudentId(student.getId())
                .stream().map(bookingMapper::toResponse).toList();
    }

    public void deleteById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (booking.getInterestList().getListStatus() != ListStatus.OPEN) {
            throw new IllegalStateException("Cannot delete booking for a closed interest list");
        }

        bookingRepository.delete(booking);
    }

    private Student getStudentByAuthentication(Authentication authentication) {
        return studentRepository.findByUser_Cpf(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    }
}
