package com.guilhermesemog.unimove.controller;

import java.util.UUID;
import com.guilhermesemog.unimove.dto.boardingstop.BoardingStopResponse;
import com.guilhermesemog.unimove.dto.conductor.ConductorCreate;
import com.guilhermesemog.unimove.dto.conductor.ConductorPatch;
import com.guilhermesemog.unimove.dto.conductor.ConductorResponse;
import com.guilhermesemog.unimove.dto.conductor.ConductorUpdate;
import com.guilhermesemog.unimove.service.ConductorService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/conductors")
public class ConductorController {

    private final ConductorService conductorService;

    public ConductorController(ConductorService conductorService) {
        this.conductorService = conductorService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConductorResponse> save(@RequestBody ConductorCreate requestBody) {
        ConductorResponse responseBody = conductorService.create(requestBody);
        return ResponseEntity.status(201).body(responseBody);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isOwner(#id, authentication)")
    public ResponseEntity<ConductorResponse> getById(@PathVariable UUID id) {
        ConductorResponse responseBody = conductorService.getById(id);
        return ResponseEntity.ok(responseBody);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ConductorResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        Page<ConductorResponse> responseBody = conductorService.getAll(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(responseBody);
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ConductorResponse>> getAll() {
        List<ConductorResponse> responseBody = conductorService.getAll();
        return ResponseEntity.ok(responseBody);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        conductorService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> update(@PathVariable UUID id, @Valid @RequestBody ConductorUpdate requestBody) {
        conductorService.update(id, requestBody);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> update(@PathVariable UUID id, @Valid @RequestBody ConductorPatch requestBody) {
        conductorService.update(id, requestBody);
        return ResponseEntity.ok().build();
    }

}
