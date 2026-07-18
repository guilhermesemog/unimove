package com.guilhermesemog.unimove.controller;

import com.guilhermesemog.unimove.dto.interestlist.*;
import com.guilhermesemog.unimove.service.InterestListService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("interest-lists")
public class InterestListController {

    private final InterestListService interestListService;

    public InterestListController(InterestListService interestListService) {
        this.interestListService = interestListService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InterestListResponse> create(@Valid @RequestBody InterestListCreate requestBody) {
        InterestListResponse responseBody = interestListService.create(requestBody);
        return ResponseEntity.status(201).body(responseBody);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InterestListResponse> getById(@PathVariable Long id) {
        InterestListResponse responseBody = interestListService.getById(id);
        return ResponseEntity.ok(responseBody);
    }

    @GetMapping
    public ResponseEntity<Page<InterestListResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        Page<InterestListResponse> responseBody = interestListService.getAll(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(responseBody);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        interestListService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody InterestListUpdate requestBody) {
        interestListService.update(id, requestBody);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody InterestListPatch requestBody) {
        interestListService.update(id, requestBody);
        return ResponseEntity.ok().build();
    }

    @PatchMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> toggleStatus(@RequestParam Long id, @Valid @RequestBody InterestListToggleStatus requestBody) {
        interestListService.toggleStatus(id, requestBody);
        return ResponseEntity.ok().build();
    }
}
