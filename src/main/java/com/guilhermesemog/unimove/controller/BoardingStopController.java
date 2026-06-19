package com.guilhermesemog.unimove.controller;

import com.guilhermesemog.unimove.dto.boardingstop.BoardingStopPostRequestBody;
import com.guilhermesemog.unimove.dto.boardingstop.BoardingStopResponseBody;
import com.guilhermesemog.unimove.service.BoardingStopService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/boarding-stops")
public class BoardingStopController {

    private final BoardingStopService boardingStopService;

    public BoardingStopController(BoardingStopService boardingStopService) {
        this.boardingStopService = boardingStopService;
    }

    @PostMapping
    public ResponseEntity<BoardingStopResponseBody> create(@RequestBody BoardingStopPostRequestBody requestBody) {
        BoardingStopResponseBody response = boardingStopService.create(requestBody);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BoardingStopResponseBody> getById(@PathVariable Long id) {
        BoardingStopResponseBody response = boardingStopService.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<BoardingStopResponseBody>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        Page<BoardingStopResponseBody> response = boardingStopService.getAll(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boardingStopService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
