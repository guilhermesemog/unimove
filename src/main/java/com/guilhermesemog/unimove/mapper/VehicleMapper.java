package com.guilhermesemog.unimove.mapper;

import com.guilhermesemog.unimove.dto.vehicle.VehiclePatchRequestBody;
import com.guilhermesemog.unimove.dto.vehicle.VehiclePostRequestBody;
import com.guilhermesemog.unimove.dto.vehicle.VehiclePutRequestBody;
import com.guilhermesemog.unimove.dto.vehicle.VehicleResponseBody;
import com.guilhermesemog.unimove.model.Vehicle;
import org.springframework.stereotype.Component;

@Component
public class VehicleMapper {
    public Vehicle toEntity(VehiclePostRequestBody vehiclePostRequestBody) {
        return new Vehicle(
                vehiclePostRequestBody.plate(),
                vehiclePostRequestBody.capacity()
        );
    }

    public VehicleResponseBody toResponseBody(Vehicle vehicle) {
        return new VehicleResponseBody(
                vehicle.getId(),
                vehicle.getPlate(),
                vehicle.getCapacity()
        );
    }

    public Vehicle updateVehicle(VehiclePatchRequestBody newVehicle, Vehicle vehicle) {
        if (newVehicle.plate() != null) {
            vehicle.setPlate(newVehicle.plate());
        }
        if (newVehicle.capacity() != null) {
            vehicle.setCapacity(newVehicle.capacity());
        }
        return vehicle;
    }

    public Vehicle updateVehicle(VehiclePutRequestBody newVehicle, Vehicle vehicle) {
        vehicle.setPlate(newVehicle.plate());
        vehicle.setCapacity(newVehicle.capacity());

        return vehicle;
    }
}
