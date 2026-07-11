package com.guilhermesemog.unimove.controller;

import com.guilhermesemog.unimove.dto.booking.BookingCreate;
import com.guilhermesemog.unimove.dto.booking.BookingResponse;
import com.guilhermesemog.unimove.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }


    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> create(Authentication authentication, @Valid @RequestBody BookingCreate requestBody) {
        bookingService.create(authentication, requestBody);
        return ResponseEntity.status(201).build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #userSecurity.isOwner(authentication, #id)")
    public ResponseEntity<BookingResponse> getById(@PathVariable Long id) {
        BookingResponse response = bookingService.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/interest-list/{interestListId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingResponse>> getAllByInterestList(@PathVariable Long interestListId) {
        List<BookingResponse> response = bookingService.getAllBookingsByInterestList(interestListId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<BookingResponse>> getAllByStudent(Authentication authentication) {
        List<BookingResponse> response = bookingService.getAllBookingsByStudent(authentication);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("#userSecurity.isOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        bookingService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
