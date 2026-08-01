package com.guilhermesemog.unimove.mapper;

import com.guilhermesemog.unimove.dto.conductor.ConductorResponse;
import com.guilhermesemog.unimove.dto.interestlist.InterestListResponse;
import com.guilhermesemog.unimove.dto.trip.TripCreate;
import com.guilhermesemog.unimove.dto.trip.TripResponse;
import com.guilhermesemog.unimove.dto.vehicle.VehicleResponse;
import com.guilhermesemog.unimove.model.InterestList;
import com.guilhermesemog.unimove.model.Trip;
import org.springframework.stereotype.Component;

@Component
public class TripMapper {

    private final InterestListMapper interestListMapper;
    private final ConductorMapper conductorMapper;
    private final VehicleMapper vehicleMapper;

    public TripMapper(InterestListMapper interestListMapper, ConductorMapper conductorMapper, VehicleMapper vehicleMapper) {
        this.interestListMapper = interestListMapper;
        this.conductorMapper = conductorMapper;
        this.vehicleMapper = vehicleMapper;
    }

    public Trip toEntity(TripCreate tripBody, InterestList interestList) {
        return new Trip(interestList);
    }

    public TripResponse toResponse(Trip trip) {
        InterestListResponse interestListResponse = interestListMapper.toResponse(trip.getInterestList());
        ConductorResponse conductorResponse = trip.getConductor() == null
                ? null : conductorMapper.toResponse(trip.getConductor());

        VehicleResponse vehicleResponse = trip.getVehicle() == null
                ? null : vehicleMapper.toResponse(trip.getVehicle());

        return new TripResponse(
                trip.getId(),
                trip.getStatus(),
                interestListResponse,
                conductorResponse,
                vehicleResponse
        );
    }
}
