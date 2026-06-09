package com.guilhermesemog.unimove.model;

import com.guilhermesemog.unimove.model.embeddable.TripBoardingPointId;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "trip_boarding_points")
public class TripBoardingPoint {

    @EmbeddedId
    private TripBoardingPointId id;

    @MapsId("tripId")
    @ManyToOne
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @MapsId("boardingStopId")
    @ManyToOne
    @JoinColumn(name = "boarding_stop_id", nullable = false)
    private BoardingStop location;
}
