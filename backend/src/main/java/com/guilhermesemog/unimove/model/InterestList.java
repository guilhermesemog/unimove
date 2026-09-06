package com.guilhermesemog.unimove.model;

import java.util.UUID;
import com.guilhermesemog.unimove.model.enums.ListStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.Instant;
import java.time.LocalTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@NoArgsConstructor
@Table(name = "interest_lists")
public class InterestList extends BaseOperationalEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

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

    @Column(nullable = false)
    private Instant statusChangedAt = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recurrence_plan_id")
    private RecurrencePlan recurrencePlan;

    private LocalDate occurrenceDate;

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
