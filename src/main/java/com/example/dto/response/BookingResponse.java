package com.example.dto.response;

import com.example.entity.BookingStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {

    private Long id;
    private String bookingReference;
    private Long userId;
    private String guestName;
    private String guestEmail;
    private Long roomId;
    private String roomNumber;
    private String roomType;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Long numberOfNights;
    private Integer numberOfGuests;
    private BigDecimal pricePerNight;
    private BigDecimal totalAmount;
    private BigDecimal advancePayment;
    private BigDecimal balanceAmount;
    private BookingStatus bookingStatus;
    private String specialRequests;
    private String cancellationReason;
    private LocalDateTime cancelledAt;
    private LocalDateTime checkedInAt;
    private LocalDateTime checkedOutAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
