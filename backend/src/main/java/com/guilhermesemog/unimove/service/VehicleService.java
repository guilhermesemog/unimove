package com.guilhermesemog.unimove.service;

import java.util.UUID;
import com.guilhermesemog.unimove.dto.vehicle.VehicleCreate;
import com.guilhermesemog.unimove.dto.vehicle.VehiclePatch;
import com.guilhermesemog.unimove.dto.vehicle.VehicleResponse;
import com.guilhermesemog.unimove.dto.vehicle.VehicleUpdate;
import com.guilhermesemog.unimove.exception.type.ResourceNotFoundException;
import com.guilhermesemog.unimove.mapper.VehicleMapper;
import com.guilhermesemog.unimove.model.Vehicle;
import com.guilhermesemog.unimove.repository.VehicleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

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
        return vehicleMapper.toResponse(vehicleRepository.save(vehicle));
    }

    public VehicleResponse getById(UUID id) {
        Vehicle vehicle = getVehicle(id);
        return vehicleMapper.toResponse(vehicle);
    }

    public Page<VehicleResponse> getAllByPlate(int page, int size, String sortBy, String sortDirection, String plate) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return vehicleRepository.findAllByPlateContainsIgnoreCase(plate, pageable).map(vehicleMapper::toResponse);
    }

    public Page<VehicleResponse> getAll(int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return vehicleRepository.findAll(pageable).map(vehicleMapper::toResponse);
    }

    public List<VehicleResponse> getAll() {
        return vehicleRepository.findAll().stream().map(vehicleMapper::toResponse).collect(toList());
    }

    public void delete(UUID id) {
        Vehicle vehicle = getVehicle(id);
        vehicleRepository.delete(vehicle);
    }

    public void update(UUID id, VehiclePatch vehiclePatch) {
        Vehicle vehicle = getVehicle(id);
        vehicle = vehicleMapper.updateVehicle(vehiclePatch, vehicle);
        vehicleRepository.save(vehicle);
    }

    public void update(UUID id, VehicleUpdate vehicleUpdate) {
        Vehicle vehicle = getVehicle(id);
        vehicle = vehicleMapper.updateVehicle(vehicleUpdate, vehicle);
        vehicleRepository.save(vehicle);
    }

    private Vehicle getVehicle(UUID id) {
        return vehicleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
    }

}
