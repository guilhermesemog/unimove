package com.guilhermesemog.unimove.controller;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.guilhermesemog.unimove.dto.student.StudentResponse;
import com.guilhermesemog.unimove.dto.driver.DriverOperationResponse;
import com.guilhermesemog.unimove.dto.trip.TripAssignmentUpdate;
import com.guilhermesemog.unimove.dto.trip.TripCreate;
import com.guilhermesemog.unimove.dto.trip.TripPatch;
import com.guilhermesemog.unimove.dto.trip.TripResponse;
import com.guilhermesemog.unimove.service.TripService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/trips")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TripResponse> create(@Valid @RequestBody TripCreate requestBody) {
        return ResponseEntity.status(201).body(tripService.create(requestBody));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<TripResponse>> getAllTrips(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        return ResponseEntity.ok(tripService.getAll(page, size, sortBy, sortDirection));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TripResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(tripService.getById(id));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT') or hasRole('CONDUCTOR')")
    public ResponseEntity<Page<TripResponse>> getAllByUser(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        return ResponseEntity.ok(tripService.getAllByUser(authentication, page, size, sortBy, sortDirection));
    }

    @GetMapping("/me/{id}/operation")
    @PreAuthorize("hasRole('CONDUCTOR')")
    public ResponseEntity<DriverOperationResponse> getDriverOperation(
            Authentication authentication,
            @PathVariable UUID id) {
        return ResponseEntity.ok(tripService.getDriverOperation(authentication, id));
    }

    @GetMapping("/{id}/students")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<StudentResponse>> getAllTripStudents(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        return ResponseEntity.ok(tripService.getAllStudentsByTrip(id, page, size, sortBy, sortDirection));
    }

    @PatchMapping("/{id}/conductor")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TripResponse> updateConductor(@PathVariable UUID id, @RequestBody TripPatch requestBody) {
        tripService.updateConductor(id, requestBody);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/vehicle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TripResponse> updateVehicle(@PathVariable UUID id, @RequestBody TripPatch requestBody) {
        tripService.updateVehicle(id, requestBody);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/assignment")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TripResponse> updateAssignment(
            @PathVariable UUID id,
            @Valid @RequestBody TripAssignmentUpdate requestBody) {
        return ResponseEntity.ok(tripService.updateAssignment(id, requestBody));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<TripResponse>> searchTrips(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(defaultValue = "") String searchTerm) {
        return ResponseEntity
                .ok(tripService.getAllTripsByConductorOrVehicle(page, size, sortBy, sortDirection, searchTerm));
    }
}
