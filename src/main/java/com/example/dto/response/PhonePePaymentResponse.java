package com.example.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhonePePaymentResponse {

    private Long paymentId;
    private String transactionId;
    private String merchantTransactionId;
    private Long bookingId;
    private String bookingReference;

    // UPI payment details
    private String upiId;           // hotel's UPI ID (payee)
    private String customerUpiId;   // customer's UPI ID (payer)
    private String upiApp;

    // QR / deep-link data
    private String upiPaymentLink;  // upi://pay?pa=...  (for QR code generation)
    private String qrContent;       // same link, used by frontend to render QR

    private BigDecimal amount;
    private String currency;
    private String paymentStatus;   // PENDING | COMPLETED | FAILED
    private String failureReason;

    private LocalDateTime initiatedAt;
    private LocalDateTime completedAt;

    // Bank account details shown on payment page
    private String bankName;
    private String accountHolderName;
    private String accountNumber;
    private String ifscCode;
    private String branchName;
}