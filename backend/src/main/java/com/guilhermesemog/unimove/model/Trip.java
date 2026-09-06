package com.guilhermesemog.unimove.model;

import java.util.UUID;
import com.guilhermesemog.unimove.model.enums.TripStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@NoArgsConstructor
@Table(name = "trips")
public class Trip extends BaseOperationalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "interest_list_id", nullable = false)
    private InterestList interestList;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TripStatus status = TripStatus.PROCESSING;

    @ManyToOne
    @JoinColumn(name = "conductor_id")
    private Conductor conductor;

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Column
    private Instant assignedAt;

    @Column(nullable = false)
    private Instant statusChangedAt = Instant.now();

    public Trip(InterestList interestList) {
        this.interestList = interestList;
    }
}
