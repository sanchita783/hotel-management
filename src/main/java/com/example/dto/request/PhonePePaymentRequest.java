package com.example.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhonePePaymentRequest {

    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Amount must be at least ₹1")
    private BigDecimal amount;

    // UPI ID of the customer (e.g. user@okaxis)
    @NotBlank(message = "Customer UPI ID is required")
    @Pattern(regexp = "^[a-zA-Z0-9.\\-_]{2,256}@[a-zA-Z]{2,64}$",
             message = "Invalid UPI ID format. Example: name@okaxis")
    private String customerUpiId;

    // Which UPI app customer is using: PHONEPE, GPAY, PAYTM, BHIM, OTHER
    @NotBlank(message = "UPI app type is required")
    private String upiApp;

    // Gateway transaction reference generated on frontend
    private String merchantTransactionId;

    private String notes;
}