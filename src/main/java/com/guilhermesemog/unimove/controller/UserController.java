package com.guilhermesemog.unimove.controller;

import com.guilhermesemog.unimove.dto.user.UserCreate;
import com.guilhermesemog.unimove.dto.user.UserPatch;
import com.guilhermesemog.unimove.dto.user.UserResponse;
import com.guilhermesemog.unimove.dto.user.UserUpdate;
import com.guilhermesemog.unimove.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreate requestBody) {
        UserResponse savedUser = userService.create(requestBody);
        return ResponseEntity.status(201).body(savedUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        UserResponse responseBody = userService.getById(id);
        return ResponseEntity.ok(responseBody);
    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        Page<UserResponse> responseBody = userService.getAll(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(responseBody);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody UserUpdate requestBody) {
        userService.update(id, requestBody);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody UserPatch requestBody) {
        userService.update(id, requestBody);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> toggleStatus(@PathVariable Long id) {
        userService.toggleStatus(id);
        return ResponseEntity.ok().build();
    }

}
