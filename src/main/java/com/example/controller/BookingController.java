package com.example.controller;

import com.example.dto.request.BookingRequest;
import com.example.dto.response.ApiResponse;
import com.example.dto.response.BookingResponse;
import com.example.entity.BookingStatus;
import com.example.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Tag(name = "Booking Management", description = "Create and manage hotel bookings")
@SecurityRequirement(name = "bearerAuth")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/create")
    @Operation(summary = "Create a new booking")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody BookingRequest request) {
        BookingResponse booking = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Booking created successfully", booking));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking by ID")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Booking retrieved",
                bookingService.getBookingById(id)));
    }

    @GetMapping("/reference/{reference}")
    @Operation(summary = "Get booking by reference number")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingByReference(
            @PathVariable String reference) {
        return ResponseEntity.ok(ApiResponse.success("Booking retrieved",
                bookingService.getBookingByReference(reference)));
    }

    @GetMapping("/my-bookings")
    @Operation(summary = "Get current user's booking history")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getMyBookings() {
        return ResponseEntity.ok(ApiResponse.success("Bookings retrieved",
                bookingService.getCurrentUserBookings()));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get bookings by user ID (Admin or own bookings)")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getBookingsByUser(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Bookings retrieved",
                bookingService.getBookingsByUser(userId)));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel a booking")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.getOrDefault("reason", "Cancelled by guest") :
                "Cancelled by guest";
        BookingResponse booking = bookingService.cancelBooking(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully", booking));
    }

    // ─── Admin Endpoints ───────────────────────────────────────────────────────

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all bookings (Admin only)")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getAllBookings() {
        return ResponseEntity.ok(ApiResponse.success("All bookings retrieved",
                bookingService.getAllBookings()));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get bookings by status (Admin only)")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getBookingsByStatus(
            @PathVariable BookingStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Bookings retrieved",
                bookingService.getBookingsByStatus(status)));
    }

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Confirm a pending booking (Admin only)")
    public ResponseEntity<ApiResponse<BookingResponse>> confirmBooking(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Booking confirmed",
                bookingService.confirmBooking(id)));
    }

    @PatchMapping("/{id}/check-in")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Check in a guest (Admin only)")
    public ResponseEntity<ApiResponse<BookingResponse>> checkIn(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Check-in successful",
                bookingService.checkIn(id)));
    }

    @PatchMapping("/{id}/check-out")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Check out a guest (Admin only)")
    public ResponseEntity<ApiResponse<BookingResponse>> checkOut(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Check-out successful",
                bookingService.checkOut(id)));
    }
}
