package com.guilhermesemog.unimove.model;

import com.guilhermesemog.unimove.model.enums.TripStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table(name = "trips")
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    public Trip(InterestList interestList) {
        this.interestList = interestList;
    }
}
