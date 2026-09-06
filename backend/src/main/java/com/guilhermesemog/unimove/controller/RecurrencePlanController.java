package com.guilhermesemog.unimove.controller;

import com.guilhermesemog.unimove.dto.recurrence.*;
import com.guilhermesemog.unimove.service.RecurrencePlanService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/recurrence-plans")
@PreAuthorize("hasRole('ADMIN')")
public class RecurrencePlanController {
    private final RecurrencePlanService recurrencePlanService;

    public RecurrencePlanController(RecurrencePlanService recurrencePlanService) {
        this.recurrencePlanService = recurrencePlanService;
    }

    @GetMapping
    public ResponseEntity<Page<RecurrencePlanResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        return ResponseEntity.ok(recurrencePlanService.getAll(page, size, sortDirection));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecurrencePlanResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(recurrencePlanService.getById(id));
    }

    @PostMapping("/preview")
    public ResponseEntity<RecurrencePreviewResponse> preview(@Valid @RequestBody RecurrencePlanRequest request) {
        return ResponseEntity.ok(recurrencePlanService.preview(request));
    }

    @PostMapping
    public ResponseEntity<RecurrencePlanResponse> create(@Valid @RequestBody RecurrencePlanRequest request) {
        return ResponseEntity.status(201).body(recurrencePlanService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecurrencePlanResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody RecurrencePlanRequest request
    ) {
        return ResponseEntity.ok(recurrencePlanService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<RecurrencePlanResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody RecurrenceStatusUpdate request
    ) {
        return ResponseEntity.ok(recurrencePlanService.updateStatus(id, request));
    }

    @PostMapping("/{id}/generate")
    public ResponseEntity<RecurrenceGenerationResponse> generate(@PathVariable UUID id) {
        return ResponseEntity.ok(recurrencePlanService.generate(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> archive(@PathVariable UUID id) {
        recurrencePlanService.archive(id);
        return ResponseEntity.noContent().build();
    }
}
