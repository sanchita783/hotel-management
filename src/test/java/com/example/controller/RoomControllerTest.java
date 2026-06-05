package com.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.dto.request.RoomRequest;
import com.example.dto.response.RoomResponse;
import com.example.entity.RoomStatus;
import com.example.entity.RoomType;
import com.example.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Room Controller Integration Tests")
class RoomControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private RoomService roomService;

    private RoomResponse sampleRoom;
    private RoomRequest roomRequest;

    @BeforeEach
    void setUp() {
        sampleRoom = RoomResponse.builder()
                .id(1L)
                .roomNumber("101")
                .roomType(RoomType.DOUBLE)
                .pricePerNight(new BigDecimal("2500.00"))
                .capacity(2)
                .floorNumber(1)
                .roomStatus(RoomStatus.AVAILABLE)
                .isActive(true)
                .build();

        roomRequest = RoomRequest.builder()
                .roomNumber("101")
                .roomType(RoomType.DOUBLE)
                .pricePerNight(new BigDecimal("2500.00"))
                .capacity(2)
                .floorNumber(1)
                .build();
    }

    @Test
    @DisplayName("GET /rooms - returns 200 with list of rooms (public)")
    void getAllRooms_ShouldReturn200_ForPublicAccess() throws Exception {
        given(roomService.getActiveRooms()).willReturn(List.of(sampleRoom));

        mockMvc.perform(get("/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].roomNumber").value("101"))
                .andExpect(jsonPath("$.data[0].roomType").value("DOUBLE"));
    }

    @Test
    @DisplayName("GET /rooms/{id} - returns room by ID (public)")
    void getRoomById_ShouldReturn200() throws Exception {
        given(roomService.getRoomById(1L)).willReturn(sampleRoom);

        mockMvc.perform(get("/rooms/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.pricePerNight").value(2500.00));
    }

    @Test
    @DisplayName("GET /rooms/available - returns available rooms (public)")
    void getAvailableRooms_ShouldReturn200() throws Exception {
        given(roomService.searchAvailableRooms(any(), any(), anyInt()))
                .willReturn(List.of(sampleRoom));

        mockMvc.perform(get("/rooms/available")
                        .param("checkIn", LocalDate.now().plusDays(1).toString())
                        .param("checkOut", LocalDate.now().plusDays(3).toString())
                        .param("guests", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /rooms/add - Admin creates room, returns 201")
    void addRoom_ShouldReturn201_WhenAdmin() throws Exception {
        given(roomService.addRoom(any(RoomRequest.class))).willReturn(sampleRoom);

        mockMvc.perform(post("/rooms/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roomRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.roomNumber").value("101"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("POST /rooms/add - Customer gets 403 Forbidden")
    void addRoom_ShouldReturn403_WhenCustomer() throws Exception {
        mockMvc.perform(post("/rooms/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roomRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /rooms/add - Unauthenticated gets 401")
    void addRoom_ShouldReturn401_WhenUnauthenticated() throws Exception {
        mockMvc.perform(post("/rooms/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roomRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /rooms/add - returns 400 when roomNumber is blank")
    void addRoom_ShouldReturn400_WhenRoomNumberMissing() throws Exception {
        roomRequest.setRoomNumber("");

        mockMvc.perform(post("/rooms/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roomRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.roomNumber").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /rooms/update/{id} - Admin updates room")
    void updateRoom_ShouldReturn200_WhenAdmin() throws Exception {
        given(roomService.updateRoom(eq(1L), any(RoomRequest.class))).willReturn(sampleRoom);

        mockMvc.perform(put("/rooms/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roomRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /rooms/delete/{id} - Admin soft deletes room")
    void deleteRoom_ShouldReturn200_WhenAdmin() throws Exception {
        willDoNothing().given(roomService).deleteRoom(1L);

        mockMvc.perform(delete("/rooms/delete/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Room deleted successfully"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PATCH /rooms/{id}/status - Admin updates room status")
    void updateRoomStatus_ShouldReturn200_WhenAdmin() throws Exception {
        sampleRoom.setRoomStatus(RoomStatus.MAINTENANCE);
        given(roomService.updateRoomStatus(1L, RoomStatus.MAINTENANCE)).willReturn(sampleRoom);

        mockMvc.perform(patch("/rooms/1/status")
                        .param("status", "MAINTENANCE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roomStatus").value("MAINTENANCE"));
    }
}
