package com.guilhermesemog.unimove.mapper;

import com.guilhermesemog.unimove.dto.vehicle.VehicleCreate;
import com.guilhermesemog.unimove.dto.vehicle.VehiclePatch;
import com.guilhermesemog.unimove.dto.vehicle.VehicleResponse;
import com.guilhermesemog.unimove.dto.vehicle.VehicleUpdate;
import com.guilhermesemog.unimove.model.Vehicle;
import org.springframework.stereotype.Component;

@Component
public class VehicleMapper {
    public Vehicle toEntity(VehicleCreate body) {
        return new Vehicle(
                body.plate(),
                body.capacity()
        );
    }

    public VehicleResponse toResponse(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getPlate(),
                vehicle.getCapacity()
        );
    }

    public Vehicle updateVehicle(VehiclePatch newVehicle, Vehicle vehicle) {
        if (newVehicle.plate() != null) {
            vehicle.setPlate(newVehicle.plate());
        }
        if (newVehicle.capacity() != null) {
            vehicle.setCapacity(newVehicle.capacity());
        }
        return vehicle;
    }

    public Vehicle updateVehicle(VehicleUpdate newVehicle, Vehicle vehicle) {
        vehicle.setPlate(newVehicle.plate());
        vehicle.setCapacity(newVehicle.capacity());

        return vehicle;
    }
}
