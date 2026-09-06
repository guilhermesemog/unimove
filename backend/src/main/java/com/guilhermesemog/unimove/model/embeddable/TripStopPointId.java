package com.guilhermesemog.unimove.model.embeddable;

import java.util.UUID;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class TripStopPointId {
    private UUID tripId;
    private UUID universityId;
}
