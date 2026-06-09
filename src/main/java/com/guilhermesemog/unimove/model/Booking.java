package com.guilhermesemog.unimove.model;

import com.guilhermesemog.unimove.model.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "interest_list_id", nullable = false)
    private InterestList interestList;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus bookingStatus = BookingStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "destination_id", nullable = false)
    private University destination;

    @ManyToOne
    @JoinColumn(name = "boarding_location_id")
    private BoardingStop boardingLocation;
}
