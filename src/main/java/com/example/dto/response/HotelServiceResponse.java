package com.example.dto.response;

import com.example.entity.ServiceStatus;
import com.example.entity.ServiceType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelServiceResponse {

    private Long id;
    private Long bookingId;
    private String bookingReference;
    private ServiceType serviceType;
    private String serviceDescription;
    private BigDecimal amount;
    private ServiceStatus serviceStatus;
    private LocalDateTime requestedAt;
    private LocalDateTime completedAt;
    private String staffNotes;
    private LocalDateTime createdAt;
}
