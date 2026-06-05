package com.example.controller;

import com.example.dto.response.ApiResponse;
import com.example.dto.response.BookingResponse;
import com.example.dto.response.DashboardResponse;
import com.example.entity.BookingStatus;
import com.example.service.BookingService;
import com.example.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin Panel", description = "Admin-only dashboard and management endpoints")
public class AdminController {

    private final DashboardService dashboardService;
    private final BookingService bookingService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get hotel dashboard statistics")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success("Dashboard data retrieved",
                dashboardService.getDashboardStats()));
    }

    @GetMapping("/bookings/today-checkins")
    @Operation(summary = "Get today's expected check-ins")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getTodayCheckIns() {
        return ResponseEntity.ok(ApiResponse.success("Today's check-ins",
                bookingService.getBookingsByStatus(BookingStatus.CONFIRMED)));
    }

    @GetMapping("/bookings/active")
    @Operation(summary = "Get all currently active bookings (checked-in)")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getActiveBookings() {
        return ResponseEntity.ok(ApiResponse.success("Active bookings retrieved",
                bookingService.getBookingsByStatus(BookingStatus.CHECKED_IN)));
    }

    @GetMapping("/bookings/pending")
    @Operation(summary = "Get all pending bookings awaiting confirmation")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getPendingBookings() {
        return ResponseEntity.ok(ApiResponse.success("Pending bookings retrieved",
                bookingService.getBookingsByStatus(BookingStatus.PENDING)));
    }
}
