package com.example.dto.response;

import com.example.entity.PaymentMethod;
import com.example.entity.PaymentStatus;
import com.example.entity.PaymentType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

    private Long id;
    private String transactionId;
    private Long bookingId;
    private String bookingReference;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private PaymentType paymentType;
    private LocalDateTime paymentDate;
    private String gatewayReference;
    private String failureReason;
    private String notes;
    private LocalDateTime createdAt;
}
