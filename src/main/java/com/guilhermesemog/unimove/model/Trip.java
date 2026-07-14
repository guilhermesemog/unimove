package com.guilhermesemog.unimove.model;

import com.guilhermesemog.unimove.model.enums.TripStatus;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "trips")
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TripStatus tripStatus = TripStatus.SCHEDULED;

    @ManyToOne
    @JoinColumn(name = "conductor_id", nullable = false)
    private Conductor conductor;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;
}
