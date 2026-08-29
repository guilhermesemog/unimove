package com.guilhermesemog.unimove.controller;

import com.guilhermesemog.unimove.dto.student.StudentCreate;
import com.guilhermesemog.unimove.dto.student.StudentPatch;
import com.guilhermesemog.unimove.dto.student.StudentResponse;
import com.guilhermesemog.unimove.dto.student.StudentUpdate;
import com.guilhermesemog.unimove.dto.student.StudentPreferredBoardingStopUpdate;
import com.guilhermesemog.unimove.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentResponse> create(@RequestBody StudentCreate requestBody) {
        StudentResponse responseBody = studentService.create(requestBody);
        return ResponseEntity.status(201).body(responseBody);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isOwner(#id, authentication)")
    public ResponseEntity<StudentResponse> getById(@PathVariable Long id) {
        StudentResponse responseBody = studentService.getById(id);
        return ResponseEntity.ok(responseBody);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<StudentResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        Page<StudentResponse> responseBody = studentService.getAll(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(responseBody);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        studentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody StudentUpdate requestBody) {
        studentService.update(id, requestBody);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody StudentPatch requestBody) {
        studentService.update(id, requestBody);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/me/preferred-boarding-stop")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentResponse> updatePreferredBoardingStop(
            Authentication authentication,
            @Valid @RequestBody StudentPreferredBoardingStopUpdate requestBody
    ) {
        return ResponseEntity.ok(studentService.updatePreferredBoardingStop(authentication, requestBody));
    }
}
