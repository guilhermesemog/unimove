package com.guilhermesemog.unimove.controller;

import com.guilhermesemog.unimove.dto.student.StudentPatchRequestBody;
import com.guilhermesemog.unimove.dto.student.StudentPostRequestBody;
import com.guilhermesemog.unimove.dto.student.StudentPutRequestBody;
import com.guilhermesemog.unimove.dto.student.StudentResponseBody;
import com.guilhermesemog.unimove.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    public ResponseEntity<StudentResponseBody> create(@RequestBody StudentPostRequestBody requestBody) {
        StudentResponseBody responseBody = studentService.create(requestBody);
        return ResponseEntity.status(201).body(responseBody);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentResponseBody> getById(@PathVariable Long id) {
        StudentResponseBody responseBody = studentService.getById(id);
        return ResponseEntity.ok(responseBody);
    }

    @GetMapping
    public ResponseEntity<Page<StudentResponseBody>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        Page<StudentResponseBody> responseBody = studentService.getAll(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(responseBody);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        studentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody StudentPutRequestBody studentPutRequestBody) {
        studentService.update(id, studentPutRequestBody);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody StudentPatchRequestBody studentPatchRequestBody) {
        studentService.update(id, studentPatchRequestBody);
        return ResponseEntity.ok().build();
    }
}
