package com.guilhermesemog.unimove.controller;

import com.guilhermesemog.unimove.dto.booking.BookingCreate;
import com.guilhermesemog.unimove.dto.booking.BookingResponse;
import com.guilhermesemog.unimove.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isOwner(#id, authentication)")
    public ResponseEntity<BookingResponse> getById(@PathVariable Long id) {
        BookingResponse response = bookingService.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/interest-list/{interestListId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<BookingResponse>> getAllByInterestList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @PathVariable Long interestListId
    ) {
        Page<BookingResponse> response = bookingService.getAllBookingsByInterestList(interestListId, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Page<BookingResponse>> getAllByStudent(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        Page<BookingResponse> response = bookingService.getAllBookingsByStudent(authentication, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@userSecurity.isOwner(#id, authentication) or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        bookingService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
