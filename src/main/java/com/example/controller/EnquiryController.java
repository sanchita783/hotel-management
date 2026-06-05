package com.example.controller;

import com.example.dto.request.EnquiryRequest;
import com.example.dto.request.EnquiryResponseRequest;
import com.example.dto.response.ApiResponse;
import com.example.dto.response.EnquiryResponse;
import com.example.entity.EnquiryStatus;
import com.example.service.EnquiryService;
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

@RestController
@RequestMapping("/enquiries")
@RequiredArgsConstructor
@Tag(name = "Enquiry System", description = "Submit and manage customer enquiries")
public class EnquiryController {

    private final EnquiryService enquiryService;

    @PostMapping("/submit")
    @Operation(summary = "Submit an enquiry (Public - no auth required)")
    public ResponseEntity<ApiResponse<EnquiryResponse>> submitEnquiry(
            @Valid @RequestBody EnquiryRequest request) {
        EnquiryResponse enquiry = enquiryService.submitEnquiry(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Enquiry submitted successfully. We'll respond within 24-48 hours.",
                        enquiry));
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get enquiry by ID")
    public ResponseEntity<ApiResponse<EnquiryResponse>> getEnquiryById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Enquiry retrieved",
                enquiryService.getEnquiryById(id)));
    }

    @GetMapping("/my-enquiries")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get current user's enquiries")
    public ResponseEntity<ApiResponse<List<EnquiryResponse>>> getMyEnquiries() {
        return ResponseEntity.ok(ApiResponse.success("Enquiries retrieved",
                enquiryService.getEnquiriesByCurrentUser()));
    }

    // ─── Admin Endpoints ───────────────────────────────────────────────────────

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get all enquiries (Admin only)")
    public ResponseEntity<ApiResponse<List<EnquiryResponse>>> getAllEnquiries() {
        return ResponseEntity.ok(ApiResponse.success("All enquiries retrieved",
                enquiryService.getAllEnquiries()));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get enquiries by status (Admin only)")
    public ResponseEntity<ApiResponse<List<EnquiryResponse>>> getEnquiriesByStatus(
            @PathVariable EnquiryStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Enquiries retrieved",
                enquiryService.getEnquiriesByStatus(status)));
    }

    @PostMapping("/respond/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Respond to an enquiry (Admin only)")
    public ResponseEntity<ApiResponse<EnquiryResponse>> respondToEnquiry(
            @PathVariable Long id,
            @Valid @RequestBody EnquiryResponseRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Response sent successfully",
                enquiryService.respondToEnquiry(id, request)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update enquiry status (Admin only)")
    public ResponseEntity<ApiResponse<EnquiryResponse>> updateEnquiryStatus(
            @PathVariable Long id,
            @RequestParam EnquiryStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Enquiry status updated",
                enquiryService.updateEnquiryStatus(id, status)));
    }
}
