package com.example.controller;

import com.example.dto.request.RoomRequest;
import com.example.dto.response.ApiResponse;
import com.example.dto.response.RoomResponse;
import com.example.entity.RoomStatus;
import com.example.entity.RoomType;
import com.example.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
@Tag(name = "Room Management", description = "Manage hotel rooms and availability")
public class RoomController {

    private final RoomService roomService;

    // ─── Public Endpoints ──────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Get all active rooms (Public)")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getAllRooms() {
        return ResponseEntity.ok(ApiResponse.success("Rooms retrieved", roomService.getActiveRooms()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get room details by ID (Public)")
    public ResponseEntity<ApiResponse<RoomResponse>> getRoomById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Room retrieved", roomService.getRoomById(id)));
    }

    @GetMapping("/number/{roomNumber}")
    @Operation(summary = "Get room by room number (Public)")
    public ResponseEntity<ApiResponse<RoomResponse>> getRoomByNumber(
            @PathVariable String roomNumber) {
        return ResponseEntity.ok(ApiResponse.success("Room retrieved",
                roomService.getRoomByNumber(roomNumber)));
    }

    @GetMapping("/type/{roomType}")
    @Operation(summary = "Get rooms by type (Public)")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getRoomsByType(
            @PathVariable RoomType roomType) {
        return ResponseEntity.ok(ApiResponse.success("Rooms retrieved",
                roomService.getRoomsByType(roomType)));
    }

    @GetMapping("/available")
    @Operation(summary = "Search available rooms (Public)")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getAvailableRooms(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(defaultValue = "1") Integer guests,
            @RequestParam(required = false) RoomType roomType) {

        List<RoomResponse> rooms = (roomType != null)
                ? roomService.searchAvailableRoomsByType(checkIn, checkOut, guests, roomType)
                : roomService.searchAvailableRooms(checkIn, checkOut, guests);

        return ResponseEntity.ok(ApiResponse.success(
                rooms.isEmpty() ? "No rooms available for the selected dates" : "Available rooms retrieved",
                rooms));
    }

    @GetMapping("/{id}/availability")
    @Operation(summary = "Check if a specific room is available (Public)")
    public ResponseEntity<ApiResponse<Boolean>> checkRoomAvailability(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        boolean available = roomService.isRoomAvailable(id, checkIn, checkOut);
        return ResponseEntity.ok(ApiResponse.success(
                available ? "Room is available" : "Room is not available", available));
    }

    // ─── Admin Endpoints ───────────────────────────────────────────────────────

    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Add a new room (Admin only)")
    public ResponseEntity<ApiResponse<RoomResponse>> addRoom(
            @Valid @RequestBody RoomRequest request) {
        RoomResponse room = roomService.addRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Room added successfully", room));
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update room details (Admin only)")
    public ResponseEntity<ApiResponse<RoomResponse>> updateRoom(
            @PathVariable Long id,
            @Valid @RequestBody RoomRequest request) {
        RoomResponse room = roomService.updateRoom(id, request);
        return ResponseEntity.ok(ApiResponse.success("Room updated successfully", room));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Soft-delete a room (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.ok(ApiResponse.success("Room deleted successfully"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update room status (Admin only)")
    public ResponseEntity<ApiResponse<RoomResponse>> updateRoomStatus(
            @PathVariable Long id,
            @RequestParam RoomStatus status) {
        RoomResponse room = roomService.updateRoomStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Room status updated", room));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get rooms by status (Admin only)")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getRoomsByStatus(
            @PathVariable RoomStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Rooms retrieved",
                roomService.getRoomsByStatus(status)));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get all rooms including inactive (Admin only)")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getAllRoomsAdmin() {
        return ResponseEntity.ok(ApiResponse.success("All rooms retrieved", roomService.getAllRooms()));
    }
}
