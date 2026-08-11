package com.guilhermesemog.unimove.controller;

import com.guilhermesemog.unimove.dto.student.StudentResponse;
import com.guilhermesemog.unimove.dto.trip.TripCreate;
import com.guilhermesemog.unimove.dto.trip.TripPatch;
import com.guilhermesemog.unimove.dto.trip.TripResponse;
import com.guilhermesemog.unimove.service.TripService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trips")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<TripResponse> create(@Valid @RequestBody TripCreate requestBody) {
        return ResponseEntity.status(201).body(tripService.create(requestBody));
    }

    @GetMapping
    public ResponseEntity<Page<TripResponse>> getAllTrips(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        return ResponseEntity.ok(tripService.getAll(page, size, sortBy, sortDirection));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TripResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(tripService.getById(id));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ROLE_STUDENT') or hasRole('ROLE_CONDUCTOR')")
    public ResponseEntity<Page<TripResponse>> getAllByUser(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        return ResponseEntity.ok(tripService.getAllByUser(authentication, page, size, sortBy, sortDirection));
    }

    @GetMapping("/{id}/students")
    public ResponseEntity<Page<StudentResponse>> getAllTripStudents(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        return ResponseEntity.ok(tripService.getAllStudentsByTrip(id, page, size, sortBy, sortDirection));
    }

    @PatchMapping("/{id}/conductor")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<TripResponse> updateConductor(@PathVariable Long id, @RequestBody TripPatch requestBody) {
        tripService.updateConductor(id, requestBody);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/vehicle")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<TripResponse> updateVehicle(@PathVariable Long id, @RequestBody TripPatch requestBody) {
        tripService.updateVehicle(id, requestBody);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Page<TripResponse>> searchTrips(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(defaultValue = "") String searchTerm
    ) {
        return ResponseEntity.ok(tripService.getAllTripsByConductorOrVehicle(page, size, sortBy, sortDirection, searchTerm));
    }
}
