package com.guilhermesemog.unimove.controller;

import com.guilhermesemog.unimove.dto.admin.AdminOperationResponse;
import com.guilhermesemog.unimove.service.AdminOperationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/operations")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOperationController {

    private final AdminOperationService adminOperationService;

    public AdminOperationController(AdminOperationService adminOperationService) {
        this.adminOperationService = adminOperationService;
    }

    @GetMapping
    public ResponseEntity<List<AdminOperationResponse>> getAll() {
        return ResponseEntity.ok(adminOperationService.getAll());
    }
}
