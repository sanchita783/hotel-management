package com.example.dto.response;

import com.example.entity.RoomStatus;
import com.example.entity.RoomType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomResponse {

    private Long id;
    private String roomNumber;
    private RoomType roomType;
    private BigDecimal pricePerNight;
    private Integer capacity;
    private String description;
    private Integer floorNumber;
    private Double roomSize;
    private List<String> amenities;
    private List<String> images;
    private RoomStatus roomStatus;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
