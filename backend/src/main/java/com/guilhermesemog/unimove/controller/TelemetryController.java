package com.guilhermesemog.unimove.controller;

import com.guilhermesemog.unimove.dto.telemetry.TelemetryAcceptedResponse;
import com.guilhermesemog.unimove.dto.telemetry.TelemetryBatchRequest;
import com.guilhermesemog.unimove.dto.telemetry.TelemetrySummaryResponse;
import com.guilhermesemog.unimove.model.enums.Role;
import com.guilhermesemog.unimove.service.TelemetryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class TelemetryController {
    private final TelemetryService telemetryService;

    public TelemetryController(TelemetryService telemetryService) {
        this.telemetryService = telemetryService;
    }

    @PostMapping("/telemetry/events")
    public ResponseEntity<TelemetryAcceptedResponse> ingest(@Valid @RequestBody TelemetryBatchRequest batch,
                                                             Authentication authentication) {
        Role role = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replaceFirst("^ROLE_", ""))
                .map(this::role)
                .flatMap(java.util.Optional::stream)
                .findFirst()
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.FORBIDDEN, "Authenticated role cannot send telemetry"));
        int accepted = telemetryService.ingest(authentication.getName(), role, batch);
        return ResponseEntity.accepted().body(new TelemetryAcceptedResponse(accepted));
    }

    @GetMapping("/admin/telemetry/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TelemetrySummaryResponse> summary(@RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(telemetryService.summary(days));
    }

    private java.util.Optional<Role> role(String value) {
        try { return java.util.Optional.of(Role.valueOf(value)); }
        catch (IllegalArgumentException exception) { return java.util.Optional.empty(); }
    }
}
