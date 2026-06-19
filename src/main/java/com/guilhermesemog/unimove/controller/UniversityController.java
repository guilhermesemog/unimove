package com.guilhermesemog.unimove.controller;

import com.guilhermesemog.unimove.dto.university.UniversityPatchRequestBody;
import com.guilhermesemog.unimove.dto.university.UniversityPostRequestBody;
import com.guilhermesemog.unimove.dto.university.UniversityPutRequestBody;
import com.guilhermesemog.unimove.dto.university.UniversityResponseBody;
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
    public ResponseEntity<UniversityResponseBody> create(@Valid @RequestBody UniversityPostRequestBody universityPostRequestBody) {
        UniversityResponseBody savedUniversity = universityService.create(universityPostRequestBody);
        return ResponseEntity.status(201).body(savedUniversity);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UniversityResponseBody> getById(@PathVariable Long id) {
        UniversityResponseBody university = universityService.getById(id);
        return ResponseEntity.ok(university);
    }

    @GetMapping("/query")
    public ResponseEntity<UniversityResponseBody> getByName(@NotBlank @RequestParam String name) {
        UniversityResponseBody university = universityService.getByName(name);
        return ResponseEntity.ok(university);
    }

    @GetMapping
    public ResponseEntity<Page<UniversityResponseBody>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        Page<UniversityResponseBody> response = universityService.getAll(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        universityService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody UniversityPutRequestBody universityPutRequestBody) {
        universityService.update(id, universityPutRequestBody);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody UniversityPatchRequestBody universityPatchRequestBody) {
        universityService.update(id, universityPatchRequestBody);
        return ResponseEntity.ok().build();
    }
}
