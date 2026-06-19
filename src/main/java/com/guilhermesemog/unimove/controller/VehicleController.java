package com.guilhermesemog.unimove.controller;

import com.guilhermesemog.unimove.dto.vehicle.VehiclePatchRequestBody;
import com.guilhermesemog.unimove.dto.vehicle.VehiclePostRequestBody;
import com.guilhermesemog.unimove.dto.vehicle.VehiclePutRequestBody;
import com.guilhermesemog.unimove.dto.vehicle.VehicleResponseBody;
import com.guilhermesemog.unimove.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping
    public ResponseEntity<VehicleResponseBody> create(@Valid @RequestBody VehiclePostRequestBody vehiclePostRequestBody) {
        VehicleResponseBody savedVehicle = vehicleService.create(vehiclePostRequestBody);
        return ResponseEntity.status(201).body(savedVehicle);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponseBody> getById(@PathVariable Long id) {
        VehicleResponseBody vehicle = vehicleService.getById(id);
        return ResponseEntity.ok(vehicle);
    }

    @GetMapping("/plate/{plate}")
    public ResponseEntity<VehicleResponseBody> getByPlate(@PathVariable String plate) {
        VehicleResponseBody vehicle = vehicleService.getByPlate(plate);
        return ResponseEntity.ok(vehicle);
    }

    @GetMapping
    public ResponseEntity<Page<VehicleResponseBody>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        Page<VehicleResponseBody> vehicles = vehicleService.getAll(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(vehicles);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vehicleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody VehiclePutRequestBody vehiclePutRequestBody) {
        vehicleService.update(id, vehiclePutRequestBody);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody VehiclePatchRequestBody vehiclePatchRequestBody) {
        vehicleService.update(id, vehiclePatchRequestBody);
        return ResponseEntity.noContent().build();
    }
}
