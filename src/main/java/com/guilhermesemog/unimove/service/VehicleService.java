package com.guilhermesemog.unimove.service;

import com.guilhermesemog.unimove.dto.vehicle.VehicleCreate;
import com.guilhermesemog.unimove.dto.vehicle.VehiclePatch;
import com.guilhermesemog.unimove.dto.vehicle.VehicleResponse;
import com.guilhermesemog.unimove.dto.vehicle.VehicleUpdate;
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

    public VehicleResponse create(VehicleCreate vehicleCreate) {
        Vehicle vehicle = vehicleMapper.toEntity(vehicleCreate);
        return vehicleMapper.toResponseBody(vehicleRepository.save(vehicle));
    }

    public VehicleResponse getById(Long id) {
        Vehicle vehicle = getVehicle(id);
        return vehicleMapper.toResponseBody(vehicle);
    }

    public VehicleResponse getByPlate(String plate) {
        Vehicle vehicle = getVehicle(plate);
        return vehicleMapper.toResponseBody(vehicle);
    }

    public Page<VehicleResponse> getAll(int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return vehicleRepository.findAll(pageable).map(vehicleMapper::toResponseBody);
    }

    public void delete(Long id) {
        Vehicle vehicle = getVehicle(id);
        vehicleRepository.delete(vehicle);
    }

    public void update(Long id, VehiclePatch vehiclePatch) {
        Vehicle vehicle = getVehicle(id);
        vehicle = vehicleMapper.updateVehicle(vehiclePatch, vehicle);
        vehicleRepository.save(vehicle);
    }

    public void update(Long id, VehicleUpdate vehicleUpdate) {
        Vehicle vehicle = getVehicle(id);
        vehicle = vehicleMapper.updateVehicle(vehicleUpdate, vehicle);
        vehicleRepository.save(vehicle);
    }

    private Vehicle getVehicle(Long id) {
        return vehicleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
    }

    private Vehicle getVehicle(String plate) {
        return vehicleRepository.findByPlate(plate).orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
    }

}
