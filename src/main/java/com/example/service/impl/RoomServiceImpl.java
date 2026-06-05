package com.example.service.impl;

import com.example.dto.request.RoomRequest;
import com.example.dto.response.RoomResponse;
import com.example.entity.Room;
import com.example.entity.RoomStatus;
import com.example.entity.RoomType;
import com.example.exception.DuplicateResourceException;
import com.example.exception.ResourceNotFoundException;
import com.example.repository.RoomRepository;
import com.example.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

    @Override
    public RoomResponse addRoom(RoomRequest request) {
        if (roomRepository.existsByRoomNumber(request.getRoomNumber())) {
            throw new DuplicateResourceException("Room number already exists: " + request.getRoomNumber());
        }

        Room room = Room.builder()
                .roomNumber(request.getRoomNumber())
                .roomType(request.getRoomType())
                .pricePerNight(request.getPricePerNight())
                .capacity(request.getCapacity())
                .description(request.getDescription())
                .floorNumber(request.getFloorNumber())
                .roomSize(request.getRoomSize())
                .amenities(request.getAmenities())
                .images(request.getImages())
                .roomStatus(request.getRoomStatus() != null ? request.getRoomStatus() : RoomStatus.AVAILABLE)
                .isActive(true)
                .build();

        room = roomRepository.save(room);
        log.info("Room added: {}", room.getRoomNumber());
        return mapToResponse(room);
    }

    @Override
    public RoomResponse updateRoom(Long roomId, RoomRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", roomId));

        if (!room.getRoomNumber().equals(request.getRoomNumber()) &&
                roomRepository.existsByRoomNumber(request.getRoomNumber())) {
            throw new DuplicateResourceException("Room number already exists: " + request.getRoomNumber());
        }

        room.setRoomNumber(request.getRoomNumber());
        room.setRoomType(request.getRoomType());
        room.setPricePerNight(request.getPricePerNight());
        room.setCapacity(request.getCapacity());
        room.setDescription(request.getDescription());
        room.setFloorNumber(request.getFloorNumber());
        room.setRoomSize(request.getRoomSize());
        room.setAmenities(request.getAmenities());
        room.setImages(request.getImages());
        if (request.getRoomStatus() != null) {
            room.setRoomStatus(request.getRoomStatus());
        }

        room = roomRepository.save(room);
        log.info("Room updated: {}", room.getRoomNumber());
        return mapToResponse(room);
    }

    @Override
    public void deleteRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", roomId));
        room.setIsActive(false);
        roomRepository.save(room);
        log.info("Room soft-deleted: {}", room.getRoomNumber());
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponse getRoomById(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", roomId));
        return mapToResponse(room);
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponse getRoomByNumber(String roomNumber) {
        Room room = roomRepository.findByRoomNumber(roomNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "roomNumber", roomNumber));
        return mapToResponse(room);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getActiveRooms() {
        return roomRepository.findByIsActive(true).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getRoomsByType(RoomType roomType) {
        return roomRepository.findByRoomType(roomType).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getRoomsByStatus(RoomStatus status) {
        return roomRepository.findByRoomStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> searchAvailableRooms(LocalDate checkIn, LocalDate checkOut, Integer guests) {
        validateDateRange(checkIn, checkOut);
        return roomRepository.findAvailableRooms(checkIn, checkOut, guests).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> searchAvailableRoomsByType(LocalDate checkIn, LocalDate checkOut,
                                                          Integer guests, RoomType roomType) {
        validateDateRange(checkIn, checkOut);
        return roomRepository.findAvailableRoomsByType(checkIn, checkOut, guests, roomType).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        return roomRepository.isRoomAvailable(roomId, checkIn, checkOut);
    }

    @Override
    public RoomResponse updateRoomStatus(Long roomId, RoomStatus status) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", roomId));
        room.setRoomStatus(status);
        room = roomRepository.save(room);
        log.info("Room {} status updated to {}", room.getRoomNumber(), status);
        return mapToResponse(room);
    }

    private void validateDateRange(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn.isAfter(checkOut) || checkIn.isEqual(checkOut)) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Check-in date cannot be in the past");
        }
    }

    private RoomResponse mapToResponse(Room room) {
        return RoomResponse.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .roomType(room.getRoomType())
                .pricePerNight(room.getPricePerNight())
                .capacity(room.getCapacity())
                .description(room.getDescription())
                .floorNumber(room.getFloorNumber())
                .roomSize(room.getRoomSize())
                .amenities(room.getAmenities())
                .images(room.getImages())
                .roomStatus(room.getRoomStatus())
                .isActive(room.getIsActive())
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .build();
    }
}
