package com.guilhermesemog.unimove.controller;

import com.guilhermesemog.unimove.dto.analytics.OperationalAnalyticsResponse;
import com.guilhermesemog.unimove.service.OperationalAnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/admin/analytics")
@PreAuthorize("hasRole('ADMIN')")
public class OperationalAnalyticsController {
    private final OperationalAnalyticsService analyticsService;

    public OperationalAnalyticsController(OperationalAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping
    public ResponseEntity<OperationalAnalyticsResponse> get(
            @RequestParam(required = false) Integer days,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(analyticsService.get(days, from, to));
    }
}
