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
    public ResponseEntity<VehicleResponseBody> createVehicle(@Valid @RequestBody VehiclePostRequestBody vehiclePostRequestBody) {
        VehicleResponseBody savedVehicle = vehicleService.createVehicle(vehiclePostRequestBody);
        return ResponseEntity.status(201).body(savedVehicle);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponseBody> getVehicleById(@PathVariable Long id) {
        VehicleResponseBody vehicle = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(vehicle);
    }

    @GetMapping("/plate/{plate}")
    public ResponseEntity<VehicleResponseBody> getVehicleByPlate(@PathVariable String plate) {
        VehicleResponseBody vehicle = vehicleService.getVehicleByPlate(plate);
        return ResponseEntity.ok(vehicle);
    }

    @GetMapping
    public ResponseEntity<Page<VehicleResponseBody>> getAllVehicles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        Page<VehicleResponseBody> vehicles = vehicleService.getAllVehicles(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(vehicles);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicleById(@PathVariable Long id) {
        vehicleService.deleteVehicleById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateVehicleById(@PathVariable Long id, @Valid @RequestBody VehiclePutRequestBody vehiclePutRequestBody) {
        vehicleService.updateVehicleById(id, vehiclePutRequestBody);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateVehicleById(@PathVariable Long id, @Valid @RequestBody VehiclePatchRequestBody vehiclePatchRequestBody) {
        vehicleService.updateVehicleById(id, vehiclePatchRequestBody);
        return ResponseEntity.noContent().build();
    }
}
