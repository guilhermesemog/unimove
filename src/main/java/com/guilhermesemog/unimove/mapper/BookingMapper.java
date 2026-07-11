package com.guilhermesemog.unimove.mapper;

import com.guilhermesemog.unimove.dto.booking.BookingResponse;
import com.guilhermesemog.unimove.model.*;
import com.guilhermesemog.unimove.model.enums.BookingStatus;
import com.guilhermesemog.unimove.model.enums.TripType;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {
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

    public BookingResponse toResponse(Booking booking) {
        return new BookingResponse(booking.getStudent().getId());
    }
}
