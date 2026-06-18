package com.guilhermesemog.unimove.service;

import com.guilhermesemog.unimove.dto.vehicle.VehiclePatchRequestBody;
import com.guilhermesemog.unimove.dto.vehicle.VehiclePostRequestBody;
import com.guilhermesemog.unimove.dto.vehicle.VehiclePutRequestBody;
import com.guilhermesemog.unimove.dto.vehicle.VehicleResponseBody;
import com.guilhermesemog.unimove.exception.ResourceNotFoundException;
import com.guilhermesemog.unimove.mapper.VehicleMapper;
import com.guilhermesemog.unimove.model.Vehicle;
import com.guilhermesemog.unimove.repository.VehicleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class VehicleService {
    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;

    public VehicleService(VehicleRepository vehicleRepository, VehicleMapper vehicleMapper) {
        this.vehicleRepository = vehicleRepository;
        this.vehicleMapper = vehicleMapper;
    }

    public VehicleResponseBody createVehicle(VehiclePostRequestBody vehiclePostRequestBody) {
        Vehicle vehicle = vehicleMapper.toEntity(vehiclePostRequestBody);
        return vehicleMapper.toResponseBody(vehicleRepository.save(vehicle));
    }

    public VehicleResponseBody getVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
        return vehicleMapper.toResponseBody(vehicle);
    }

    public VehicleResponseBody getVehicleByPlate(String plate) {
        Vehicle vehicle = vehicleRepository.findByPlate(plate).orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
        return vehicleMapper.toResponseBody(vehicle);
    }

    public Page<VehicleResponseBody> getAllVehicles(int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return vehicleRepository.findAll(pageable).map(vehicleMapper::toResponseBody);
    }

    public void deleteVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
        vehicleRepository.delete(vehicle);
    }

    public void updateVehicleById(Long id, VehiclePatchRequestBody vehiclePatchRequestBody) {
        Vehicle vehicle = vehicleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
        vehicle = vehicleMapper.updateVehicle(vehiclePatchRequestBody, vehicle);
        vehicleRepository.save(vehicle);
    }

    public void updateVehicleById(Long id, VehiclePutRequestBody vehiclePutRequestBody) {
        Vehicle vehicle = vehicleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
        vehicle = vehicleMapper.updateVehicle(vehiclePutRequestBody, vehicle);
        vehicleRepository.save(vehicle);
    }

}
