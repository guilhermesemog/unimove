package com.guilhermesemog.unimove.mapper;

import com.guilhermesemog.unimove.dto.booking.BookingResponse;
import com.guilhermesemog.unimove.model.*;
import com.guilhermesemog.unimove.model.enums.BookingStatus;
import com.guilhermesemog.unimove.model.enums.TripType;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    private final StudentMapper studentMapper;
    private final InterestListMapper interestListMapper;
    private final UniversityMapper universityMapper;
    private final BoardingStopMapper boardingStopMapper;

    public BookingMapper(
            StudentMapper studentMapper, InterestListMapper interestListMapper,
            UniversityMapper universityMapper, BoardingStopMapper boardingStopMapper
    ) {
        this.studentMapper = studentMapper;
        this.interestListMapper = interestListMapper;
        this.universityMapper = universityMapper;
        this.boardingStopMapper = boardingStopMapper;
    }

    public Booking toEntity(
            Student student,
            InterestList interestList,
            BookingStatus bookingStatus,
            TripType tripType,
            University university,
            BoardingStop boardingStop
    ) {
        return new Booking(
                student,
                interestList,
                bookingStatus,
                tripType,
                university,
                boardingStop
        );
    }

    public BookingResponse toResponse(
            Booking booking,
            Student student,
            InterestList interestList,
            University university,
            BoardingStop boardingStop
    ) {
        return new BookingResponse(
                booking.getId(),
                booking.getTripType(),
                booking.getBookingStatus(),
                studentMapper.toResponse(student),
                interestListMapper.toResponse(interestList),
                universityMapper.toResponse(university),
                boardingStopMapper.toResponse(boardingStop)
        );
    }
}
