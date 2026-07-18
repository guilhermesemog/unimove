package com.guilhermesemog.unimove.model;

import com.guilhermesemog.unimove.model.embeddable.TripInterestListId;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "trip_interest_lists")
public class TripInterestList {

    @EmbeddedId
    private TripInterestListId id;

    @MapsId("tripId")
    @ManyToOne
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @MapsId("interestListId")
    @ManyToOne
    @JoinColumn(name = "interest_list_id", nullable = false)
    private InterestList interestList;
}