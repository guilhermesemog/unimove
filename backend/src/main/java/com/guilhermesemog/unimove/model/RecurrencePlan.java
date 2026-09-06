package com.guilhermesemog.unimove.model;

import com.guilhermesemog.unimove.model.enums.RecurrencePlanStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "recurrence_plans")
public class RecurrencePlan extends BaseOperationalEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 120)
    private String name;

    @ManyToOne(optional = false)
    @JoinColumn(name = "destination_id", nullable = false)
    private University destination;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "recurrence_plan_days", joinColumns = @JoinColumn(name = "recurrence_plan_id"))
    @Column(name = "day_of_week", nullable = false, length = 12)
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> daysOfWeek = new LinkedHashSet<>();

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private LocalTime closingTime;

    @Column(nullable = false)
    private LocalTime departureTime;

    @Column(nullable = false)
    private LocalTime arrivalTime;

    @Column(nullable = false)
    private LocalTime returnDepartureTime;

    @Column(nullable = false)
    private LocalTime returnArrivalTime;

    @Column(nullable = false)
    private Integer horizonWeeks;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RecurrencePlanStatus status = RecurrencePlanStatus.ACTIVE;

    private LocalDate nextEligibleDate;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;
}
