package com.guilhermesemog.unimove.controller;

import com.guilhermesemog.unimove.dto.boardingstop.BoardingStopCreate;
import com.guilhermesemog.unimove.dto.boardingstop.BoardingStopResponse;
import com.guilhermesemog.unimove.service.BoardingStopService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/boarding-stops")
public class BoardingStopController {

    private final BoardingStopService boardingStopService;

    public BoardingStopController(BoardingStopService boardingStopService) {
        this.boardingStopService = boardingStopService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BoardingStopResponse> create(@RequestBody BoardingStopCreate requestBody) {
        BoardingStopResponse responseBody = boardingStopService.create(requestBody);
        return ResponseEntity.status(201).body(responseBody);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BoardingStopResponse> getById(@PathVariable Long id) {
        BoardingStopResponse responseBody = boardingStopService.getById(id);
        return ResponseEntity.ok(responseBody);
    }

    @GetMapping
    public ResponseEntity<Page<BoardingStopResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        Page<BoardingStopResponse> responseBody = boardingStopService.getAll(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(responseBody);
    }

    @GetMapping("/list")
    public ResponseEntity<List<BoardingStopResponse>> getAll() {
        List<BoardingStopResponse> responseBody = boardingStopService.getAll();
        return ResponseEntity.ok(responseBody);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boardingStopService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
