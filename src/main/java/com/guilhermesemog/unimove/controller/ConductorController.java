package com.guilhermesemog.unimove.controller;

import com.guilhermesemog.unimove.dto.conductor.ConductorCreate;
import com.guilhermesemog.unimove.dto.conductor.ConductorPatch;
import com.guilhermesemog.unimove.dto.conductor.ConductorResponse;
import com.guilhermesemog.unimove.dto.conductor.ConductorUpdate;
import com.guilhermesemog.unimove.service.ConductorService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/conductors")
public class ConductorController {

    private final ConductorService conductorService;

    public ConductorController(ConductorService conductorService) {
        this.conductorService = conductorService;
    }

    @PostMapping
    public ResponseEntity<ConductorResponse> save(@RequestBody ConductorCreate requestBody) {
        ConductorResponse responseBody = conductorService.create(requestBody);
        return ResponseEntity.status(201).body(responseBody);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConductorResponse> getById(@PathVariable Long id) {
        ConductorResponse responseBody = conductorService.getById(id);
        return ResponseEntity.ok(responseBody);
    }

    @GetMapping
    public ResponseEntity<Page<ConductorResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        Page<ConductorResponse> responseBody = conductorService.getAll(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(responseBody);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        conductorService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody ConductorUpdate requestBody) {
        conductorService.update(id, requestBody);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody ConductorPatch requestBody) {
        conductorService.update(id, requestBody);
        return ResponseEntity.ok().build();
    }

}
