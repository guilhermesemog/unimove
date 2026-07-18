package com.guilhermesemog.unimove.model;

import com.guilhermesemog.unimove.model.enums.ListStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Entity
@NoArgsConstructor
@Table(name = "interest_lists")
public class InterestList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate referenceDate = LocalDate.now();

    @Column
    private LocalTime closingTime = LocalTime.now().plusHours(6);

    @Column(nullable = false)
    private LocalTime departureTime = LocalTime.of(17, 30);

    @Column(nullable = false)
    private LocalTime arrivalTime = LocalTime.of(19, 0);

    @Column(nullable = false)
    private LocalTime returnDepartureTime = LocalTime.of(23, 0);

    @Column(nullable = false)
    private LocalTime returnArrivalTime = LocalTime.of(12, 30);

    @ManyToOne
    @JoinColumn(name = "destination_id", nullable = false)
    private University destination;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ListStatus listStatus = ListStatus.OPEN;

    public InterestList(LocalDate referenceDate, LocalTime closingTime, LocalTime departureTime, LocalTime arrivalTime, LocalTime returnDepartureTime, LocalTime returnArrivalTime, University destination) {
        this.referenceDate = referenceDate;
        this.closingTime = closingTime;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.returnDepartureTime = returnDepartureTime;
        this.returnArrivalTime = returnArrivalTime;
        this.destination = destination;
    }
}
