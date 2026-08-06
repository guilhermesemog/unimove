package com.guilhermesemog.unimove.model;

import com.guilhermesemog.unimove.model.enums.BookingStatus;
import com.guilhermesemog.unimove.model.enums.TripType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table(name = "bookings",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_booking_student_interestlist",
                        columnNames = {"student_id", "interest_list_id"}
                )
        })
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TripType tripType;

    @ManyToOne
    @JoinColumn(name = "destination_id", nullable = false)
    private University destination;

    @ManyToOne
    @JoinColumn(name = "boarding_location_id", nullable = false)
    private BoardingStop boardingLocation;

    public Booking(Student student, InterestList interestList, BookingStatus bookingStatus, TripType tripType, University destination, BoardingStop boardingLocation) {
        this.student = student;
        this.interestList = interestList;
        this.bookingStatus = bookingStatus;
        this.tripType = tripType;
        this.destination = destination;
        this.boardingLocation = boardingLocation;
    }
}
