package com.example.service;

import com.example.dto.request.RoomRequest;
import com.example.dto.response.RoomResponse;
import com.example.entity.Room;
import com.example.entity.RoomStatus;
import com.example.entity.RoomType;
import com.example.exception.DuplicateResourceException;
import com.example.exception.ResourceNotFoundException;
import com.example.repository.RoomRepository;
import com.example.service.impl.RoomServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Room Service Tests")
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private RoomServiceImpl roomService;

    private Room testRoom;
    private RoomRequest roomRequest;

    @BeforeEach
    void setUp() {
        testRoom = Room.builder()
                .id(1L)
                .roomNumber("101")
                .roomType(RoomType.DOUBLE)
                .pricePerNight(new BigDecimal("2500.00"))
                .capacity(2)
                .floorNumber(1)
                .roomSize(320.0)
                .roomStatus(RoomStatus.AVAILABLE)
                .isActive(true)
                .description("Cozy double room with garden view")
                .build();

        roomRequest = RoomRequest.builder()
                .roomNumber("101")
                .roomType(RoomType.DOUBLE)
                .pricePerNight(new BigDecimal("2500.00"))
                .capacity(2)
                .floorNumber(1)
                .roomSize(320.0)
                .description("Cozy double room with garden view")
                .build();
    }

    @Test
    @DisplayName("Should add room successfully when room number is unique")
    void addRoom_ShouldSucceed_WhenRoomNumberIsUnique() {
        given(roomRepository.existsByRoomNumber("101")).willReturn(false);
        given(roomRepository.save(any(Room.class))).willReturn(testRoom);

        RoomResponse response = roomService.addRoom(roomRequest);

        assertThat(response).isNotNull();
        assertThat(response.getRoomNumber()).isEqualTo("101");
        assertThat(response.getPricePerNight()).isEqualByComparingTo(new BigDecimal("2500.00"));
        then(roomRepository).should().save(any(Room.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when room number already exists")
    void addRoom_ShouldThrow_WhenRoomNumberDuplicate() {
        given(roomRepository.existsByRoomNumber("101")).willReturn(true);

        assertThatThrownBy(() -> roomService.addRoom(roomRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("101");

        then(roomRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("Should return room by ID")
    void getRoomById_ShouldReturnRoom_WhenExists() {
        given(roomRepository.findById(1L)).willReturn(Optional.of(testRoom));

        RoomResponse response = roomService.getRoomById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getRoomType()).isEqualTo(RoomType.DOUBLE);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when room not found")
    void getRoomById_ShouldThrow_WhenNotFound() {
        given(roomRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.getRoomById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Room");
    }

    @Test
    @DisplayName("Should return available rooms for given dates")
    void searchAvailableRooms_ShouldReturnList() {
        LocalDate checkIn  = LocalDate.now().plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(3);

        given(roomRepository.findAvailableRooms(checkIn, checkOut, 2))
                .willReturn(List.of(testRoom));

        List<RoomResponse> rooms = roomService.searchAvailableRooms(checkIn, checkOut, 2);

        assertThat(rooms).hasSize(1);
        assertThat(rooms.get(0).getRoomNumber()).isEqualTo("101");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid date range")
    void searchAvailableRooms_ShouldThrow_WhenInvalidDates() {
        LocalDate checkIn  = LocalDate.now().plusDays(3);
        LocalDate checkOut = LocalDate.now().plusDays(1);

        assertThatThrownBy(() -> roomService.searchAvailableRooms(checkIn, checkOut, 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should soft-delete room by setting isActive to false")
    void deleteRoom_ShouldSoftDelete() {
        given(roomRepository.findById(1L)).willReturn(Optional.of(testRoom));
        given(roomRepository.save(any(Room.class))).willAnswer(inv -> inv.getArgument(0));

        roomService.deleteRoom(1L);

        assertThat(testRoom.getIsActive()).isFalse();
        then(roomRepository).should().save(testRoom);
    }

    @Test
    @DisplayName("Should update room status correctly")
    void updateRoomStatus_ShouldUpdateStatus() {
        given(roomRepository.findById(1L)).willReturn(Optional.of(testRoom));
        given(roomRepository.save(any(Room.class))).willReturn(testRoom);

        RoomResponse response = roomService.updateRoomStatus(1L, RoomStatus.MAINTENANCE);

        then(roomRepository).should().save(any(Room.class));
        assertThat(testRoom.getRoomStatus()).isEqualTo(RoomStatus.MAINTENANCE);
    }

    @Test
    @DisplayName("Should return all active rooms")
    void getActiveRooms_ShouldReturnOnlyActiveRooms() {
        given(roomRepository.findByIsActive(true)).willReturn(List.of(testRoom));

        List<RoomResponse> rooms = roomService.getActiveRooms();

        assertThat(rooms).hasSize(1);
        assertThat(rooms.get(0).getIsActive()).isTrue();
    }
}
