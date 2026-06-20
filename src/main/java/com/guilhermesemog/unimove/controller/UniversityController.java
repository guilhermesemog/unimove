package com.guilhermesemog.unimove.controller;

import com.guilhermesemog.unimove.dto.university.UniversityCreate;
import com.guilhermesemog.unimove.dto.university.UniversityPatch;
import com.guilhermesemog.unimove.dto.university.UniversityResponse;
import com.guilhermesemog.unimove.dto.university.UniversityUpdate;
import com.guilhermesemog.unimove.service.UniversityService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/universities")
public class UniversityController {

    private final UniversityService universityService;

    public UniversityController(UniversityService universityService) {
        this.universityService = universityService;
    }

    @PostMapping
    public ResponseEntity<UniversityResponse> create(@Valid @RequestBody UniversityCreate requestBody) {
        UniversityResponse responseBody = universityService.create(requestBody);
        return ResponseEntity.status(201).body(responseBody);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UniversityResponse> getById(@PathVariable Long id) {
        UniversityResponse responseBody = universityService.getById(id);
        return ResponseEntity.ok(responseBody);
    }

    @GetMapping("/query")
    public ResponseEntity<UniversityResponse> getByName(@NotBlank @RequestParam String name) {
        UniversityResponse responseBody = universityService.getByName(name);
        return ResponseEntity.ok(responseBody);
    }

    @GetMapping
    public ResponseEntity<Page<UniversityResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        Page<UniversityResponse> responseBody = universityService.getAll(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(responseBody);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        universityService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody UniversityUpdate requestBody) {
        universityService.update(id, requestBody);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody UniversityPatch requestBody) {
        universityService.update(id, requestBody);
        return ResponseEntity.ok().build();
    }
}
