package com.example.dto.request;

import com.example.entity.RoomStatus;
import com.example.entity.RoomType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomRequest {

    @NotBlank(message = "Room number is required")
    @Size(max = 10, message = "Room number cannot exceed 10 characters")
    private String roomNumber;

    @NotNull(message = "Room type is required")
    private RoomType roomType;

    @NotNull(message = "Price per night is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal pricePerNight;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 10, message = "Capacity cannot exceed 10")
    private Integer capacity;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Floor number is required")
    @Min(value = 1, message = "Floor must be at least 1")
    private Integer floorNumber;

    @DecimalMin(value = "0.1", message = "Room size must be greater than 0")
    private Double roomSize;

    private List<String> amenities;

    private List<String> images;

    private RoomStatus roomStatus;
}
