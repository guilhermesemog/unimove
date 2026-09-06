package com.guilhermesemog.unimove.model;

import java.util.UUID;
import com.guilhermesemog.unimove.model.enums.BookingStatus;
import com.guilhermesemog.unimove.model.enums.TripType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@NoArgsConstructor
@Table(name = "bookings",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_booking_student_interestlist",
                        columnNames = {"student_id", "interest_list_id"}
                )
        })
public class Booking extends BaseOperationalEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

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

    @Column
    private Instant cancelledAt;

    public Booking(Student student, InterestList interestList, BookingStatus bookingStatus, TripType tripType, University destination, BoardingStop boardingLocation) {
        this.student = student;
        this.interestList = interestList;
        this.bookingStatus = bookingStatus;
        this.tripType = tripType;
        this.destination = destination;
        this.boardingLocation = boardingLocation;
    }
}
