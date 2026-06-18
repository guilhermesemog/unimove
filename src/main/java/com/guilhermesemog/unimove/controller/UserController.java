package com.guilhermesemog.unimove.controller;

import com.guilhermesemog.unimove.dto.user.UserPatchRequestBody;
import com.guilhermesemog.unimove.dto.user.UserPostRequestBody;
import com.guilhermesemog.unimove.dto.user.UserPutRequestBody;
import com.guilhermesemog.unimove.dto.user.UserResponseBody;
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
    public ResponseEntity<UserResponseBody> addUser(@Valid @RequestBody UserPostRequestBody user) {
        UserResponseBody savedUser = userService.createUser(user);
        return ResponseEntity.status(201).body(savedUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseBody> getUserById(@PathVariable Long id) {
        UserResponseBody user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<Page<UserResponseBody>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        Page<UserResponseBody> response = userService.getAllUsers(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable Long id) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateUser(@PathVariable Long id, @Valid @RequestBody UserPutRequestBody userPutRequestBody) {
        userService.updateUserById(id, userPutRequestBody);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateUser(@PathVariable Long id, @Valid @RequestBody UserPatchRequestBody userPatchRequestBody) {
        userService.updateUserById(id, userPatchRequestBody);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> toggleUserActiveStatus(@PathVariable Long id) {
        userService.toggleUserStatus(id);
        return ResponseEntity.ok().build();
    }

}
