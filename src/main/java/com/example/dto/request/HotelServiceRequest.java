package com.example.dto.request;

import com.example.entity.ServiceType;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelServiceRequest {

    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @NotNull(message = "Service type is required")
    private ServiceType serviceType;

    @NotBlank(message = "Service description is required")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String serviceDescription;
}
