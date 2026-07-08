package com.guilhermesemog.unimove.controller;

import com.guilhermesemog.unimove.dto.vehicle.VehicleCreate;
import com.guilhermesemog.unimove.dto.vehicle.VehiclePatch;
import com.guilhermesemog.unimove.dto.vehicle.VehicleResponse;
import com.guilhermesemog.unimove.dto.vehicle.VehicleUpdate;
import com.guilhermesemog.unimove.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VehicleResponse> create(@Valid @RequestBody VehicleCreate requestBody) {
        VehicleResponse responseBody = vehicleService.create(requestBody);
        return ResponseEntity.status(201).body(responseBody);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VehicleResponse> getById(@PathVariable Long id) {
        VehicleResponse responseBody = vehicleService.getById(id);
        return ResponseEntity.ok(responseBody);
    }

    @GetMapping("/plate/{plate}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VehicleResponse> getByPlate(@PathVariable String plate) {
        VehicleResponse responseBody = vehicleService.getByPlate(plate);
        return ResponseEntity.ok(responseBody);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<VehicleResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        Page<VehicleResponse> responseBody = vehicleService.getAll(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(responseBody);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vehicleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody VehicleUpdate requestBody) {
        vehicleService.update(id, requestBody);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody VehiclePatch requestBody) {
        vehicleService.update(id, requestBody);
        return ResponseEntity.noContent().build();
    }
}
