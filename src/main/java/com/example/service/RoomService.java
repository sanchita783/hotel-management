package com.example.service;

import com.example.dto.request.RoomRequest;
import com.example.dto.response.RoomResponse;
import com.example.entity.RoomStatus;
import com.example.entity.RoomType;

import java.time.LocalDate;
import java.util.List;

public interface RoomService {

    RoomResponse addRoom(RoomRequest request);

    RoomResponse updateRoom(Long roomId, RoomRequest request);

    void deleteRoom(Long roomId);

    RoomResponse getRoomById(Long roomId);

    RoomResponse getRoomByNumber(String roomNumber);

    List<RoomResponse> getAllRooms();

    List<RoomResponse> getActiveRooms();

    List<RoomResponse> getRoomsByType(RoomType roomType);

    List<RoomResponse> getRoomsByStatus(RoomStatus status);

    List<RoomResponse> searchAvailableRooms(LocalDate checkIn, LocalDate checkOut, Integer guests);

    List<RoomResponse> searchAvailableRoomsByType(LocalDate checkIn, LocalDate checkOut,
                                                   Integer guests, RoomType roomType);

    boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut);

    RoomResponse updateRoomStatus(Long roomId, RoomStatus status);
}
