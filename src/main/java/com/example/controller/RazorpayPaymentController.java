package com.example.controller;

import com.example.dto.request.RazorpayOrderRequest;
import com.example.dto.request.RazorpayVerifyPaymentRequest;
import com.example.dto.response.ApiResponse;
import com.example.dto.response.PaymentResponse;
import com.example.dto.response.RazorpayOrderResponse;
import com.example.service.impl.RazorpayPaymentServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments/razorpay")
@RequiredArgsConstructor
@Tag(name = "Razorpay Payment", description = "Razorpay order creation and payment verification")
@SecurityRequirement(name = "bearerAuth")
public class RazorpayPaymentController {

    private final RazorpayPaymentServiceImpl razorpayPaymentService;

    @PostMapping("/create-order")
    @Operation(summary = "Create Razorpay order for a booking")
    public ResponseEntity<ApiResponse<RazorpayOrderResponse>> createOrder(
            @Valid @RequestBody RazorpayOrderRequest request) {
        RazorpayOrderResponse response = razorpayPaymentService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Razorpay order created successfully", response));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify Razorpay payment signature and store payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> verifyPayment(
            @Valid @RequestBody RazorpayVerifyPaymentRequest request) {
        PaymentResponse response = razorpayPaymentService.verifyPayment(request);
        return ResponseEntity.ok(ApiResponse.success("Razorpay payment verified successfully", response));
    }
}
