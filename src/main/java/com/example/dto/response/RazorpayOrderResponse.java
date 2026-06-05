package com.example.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RazorpayOrderResponse {

    private String keyId;
    private String orderId;
    private BigDecimal amount;
    private Long amountInPaise;
    private String currency;
    private Long bookingId;
    private String bookingReference;
    private String companyName;
    private String description;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
}
