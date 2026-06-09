package com.guilhermesemog.unimove.model;

import com.guilhermesemog.unimove.model.embeddable.TripStopPointId;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "trip_stop_points")
public class TripStopPoint {

    @EmbeddedId
    private TripStopPointId id;

    @MapsId("tripId")
    @ManyToOne
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @MapsId("universityId")
    @ManyToOne
    @JoinColumn(name = "university_id", nullable = false)
    private University university;

}
