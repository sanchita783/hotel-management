package com.example.controller;

import com.example.dto.request.HotelServiceRequest;
import com.example.dto.response.ApiResponse;
import com.example.dto.response.HotelServiceResponse;
import com.example.entity.ServiceStatus;
import com.example.entity.ServiceType;
import com.example.service.HotelServiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/services")
@RequiredArgsConstructor
@Tag(name = "Hotel Services", description = "Laundry, Room Service, Restaurant, and more")
@SecurityRequirement(name = "bearerAuth")
public class HotelServiceController {

    private final HotelServiceService hotelServiceService;

    @PostMapping("/request")
    @Operation(summary = "Request a hotel service (e.g. Laundry, Room Service)")
    public ResponseEntity<ApiResponse<HotelServiceResponse>> requestService(
            @Valid @RequestBody HotelServiceRequest request) {
        HotelServiceResponse service = hotelServiceService.requestService(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Service requested successfully", service));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get hotel service by ID")
    public ResponseEntity<ApiResponse<HotelServiceResponse>> getServiceById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Service retrieved",
                hotelServiceService.getServiceById(id)));
    }

    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "Get all services for a booking")
    public ResponseEntity<ApiResponse<List<HotelServiceResponse>>> getServicesByBooking(
            @PathVariable Long bookingId) {
        return ResponseEntity.ok(ApiResponse.success("Services retrieved",
                hotelServiceService.getServicesByBooking(bookingId)));
    }

    @GetMapping("/my-services")
    @Operation(summary = "Get current user's service requests")
    public ResponseEntity<ApiResponse<List<HotelServiceResponse>>> getMyServices() {
        return ResponseEntity.ok(ApiResponse.success("Services retrieved",
                hotelServiceService.getServicesByCurrentUser()));
    }

    @DeleteMapping("/{id}/cancel")
    @Operation(summary = "Cancel a service request")
    public ResponseEntity<ApiResponse<Void>> cancelService(@PathVariable Long id) {
        hotelServiceService.cancelService(id);
        return ResponseEntity.ok(ApiResponse.success("Service cancelled successfully"));
    }

    // ─── Admin Endpoints ───────────────────────────────────────────────────────

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all service requests (Admin only)")
    public ResponseEntity<ApiResponse<List<HotelServiceResponse>>> getAllServices() {
        return ResponseEntity.ok(ApiResponse.success("All services retrieved",
                hotelServiceService.getAllServices()));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get services by status (Admin only)")
    public ResponseEntity<ApiResponse<List<HotelServiceResponse>>> getServicesByStatus(
            @PathVariable ServiceStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Services retrieved",
                hotelServiceService.getServicesByStatus(status)));
    }

    @GetMapping("/type/{serviceType}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get services by type (Admin only)")
    public ResponseEntity<ApiResponse<List<HotelServiceResponse>>> getServicesByType(
            @PathVariable ServiceType serviceType) {
        return ResponseEntity.ok(ApiResponse.success("Services retrieved",
                hotelServiceService.getServicesByType(serviceType)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update service status (Admin only)")
    public ResponseEntity<ApiResponse<HotelServiceResponse>> updateServiceStatus(
            @PathVariable Long id,
            @RequestParam ServiceStatus status,
            @RequestBody(required = false) Map<String, String> body) {
        String staffNotes = body != null ? body.get("staffNotes") : null;
        return ResponseEntity.ok(ApiResponse.success("Service status updated",
                hotelServiceService.updateServiceStatus(id, status, staffNotes)));
    }

    @PatchMapping("/{id}/amount")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Set service charge amount (Admin only)")
    public ResponseEntity<ApiResponse<HotelServiceResponse>> updateServiceAmount(
            @PathVariable Long id,
            @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(ApiResponse.success("Service amount updated",
                hotelServiceService.updateServiceAmount(id, amount)));
    }
}
