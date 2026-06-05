package com.example.service.impl;

import com.example.dto.request.RazorpayOrderRequest;
import com.example.dto.request.RazorpayVerifyPaymentRequest;
import com.example.dto.response.PaymentResponse;
import com.example.dto.response.RazorpayOrderResponse;
import com.example.entity.Booking;
import com.example.entity.BookingStatus;
import com.example.entity.Payment;
import com.example.entity.PaymentStatus;
import com.example.exception.PaymentException;
import com.example.exception.ResourceNotFoundException;
import com.example.repository.BookingRepository;
import com.example.repository.PaymentRepository;
import com.example.service.EmailService;
import com.example.util.TransactionIdGenerator;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RazorpayPaymentServiceImpl {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final EmailService emailService;

    @Value("${razorpay.key.id:}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret:}")
    private String razorpayKeySecret;

    @Value("${razorpay.currency:INR}")
    private String razorpayCurrency;

    @Value("${razorpay.company.name:Grand Hotel}")
    private String razorpayCompanyName;

    @Value("${razorpay.checkout.description:Hotel Booking Payment}")
    private String razorpayCheckoutDescription;

    public RazorpayOrderResponse createOrder(RazorpayOrderRequest request) {
        validateKeysConfigured();
        Booking booking = validateBookingAndAmount(request.getBookingId(), request.getAmount());

        try {
            RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", toPaise(request.getAmount()));
            orderRequest.put("currency", razorpayCurrency);
            orderRequest.put("receipt", buildReceipt(booking));

            JSONObject notes = new JSONObject();
            notes.put("booking_id", String.valueOf(booking.getId()));
            notes.put("booking_reference", booking.getBookingReference());
            notes.put("payment_method", request.getPaymentMethod().name());
            notes.put("payment_type", request.getPaymentType().name());
            if (request.getNotes() != null && !request.getNotes().isBlank()) {
                notes.put("merchant_note", request.getNotes());
            }
            orderRequest.put("notes", notes);

            Order order = client.orders.create(orderRequest);
            Long amountInPaise = ((Number) order.get("amount")).longValue();

            return RazorpayOrderResponse.builder()
                    .keyId(razorpayKeyId)
                    .orderId(order.get("id"))
                    .amount(request.getAmount())
                    .amountInPaise(amountInPaise)
                    .currency(order.get("currency"))
                    .bookingId(booking.getId())
                    .bookingReference(booking.getBookingReference())
                    .companyName(razorpayCompanyName)
                    .description(razorpayCheckoutDescription + " - " + booking.getBookingReference())
                    .customerName(booking.getUser().getFullName())
                    .customerEmail(booking.getUser().getEmail())
                    .customerPhone(booking.getUser().getPhone())
                    .build();
        } catch (Exception e) {
            log.error("Failed to create Razorpay order for booking {}", booking.getBookingReference(), e);
            throw new PaymentException("Razorpay order create karatanna error aala: " + e.getMessage());
        }
    }

    public PaymentResponse verifyPayment(RazorpayVerifyPaymentRequest request) {
        validateKeysConfigured();
        Booking booking = validateBookingAndAmount(request.getBookingId(), request.getAmount());

        if (paymentRepository.existsByGatewayReference(request.getRazorpayPaymentId())) {
            throw new PaymentException("He Razorpay payment already record zalele ahe.");
        }

        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", request.getRazorpayOrderId());
            options.put("razorpay_payment_id", request.getRazorpayPaymentId());
            options.put("razorpay_signature", request.getRazorpaySignature());

            boolean signatureValid = Utils.verifyPaymentSignature(options, razorpayKeySecret);
            if (!signatureValid) {
                throw new PaymentException("Razorpay signature verification failed.");
            }

            BigDecimal totalPaid = paymentRepository.getTotalPaidByBooking(booking.getId());
            if (totalPaid == null) totalPaid = BigDecimal.ZERO;

            BigDecimal remainingNow = booking.getTotalAmount().subtract(totalPaid)
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal requestedAmount = request.getAmount().setScale(2, RoundingMode.HALF_UP);
            if (requestedAmount.compareTo(remainingNow) > 0) {
                throw new PaymentException(
                        "Payment amount Rs." + requestedAmount + " remaining balance Rs." + remainingNow + " peksha jast ahe.");
            }

            String transactionId;
            do {
                transactionId = TransactionIdGenerator.generate();
            } while (paymentRepository.existsByTransactionId(transactionId));

            Payment payment = Payment.builder()
                    .transactionId(transactionId)
                    .booking(booking)
                    .amount(request.getAmount())
                    .paymentMethod(request.getPaymentMethod())
                    .paymentStatus(PaymentStatus.COMPLETED)
                    .paymentType(request.getPaymentType())
                    .paymentDate(LocalDateTime.now())
                    .gatewayReference(request.getRazorpayPaymentId())
                    .notes(buildNotes(request))
                    .build();

            payment = paymentRepository.save(payment);

            BigDecimal newTotalPaid = totalPaid.add(request.getAmount());
            booking.setAdvancePayment(newTotalPaid);
            booking.setBalanceAmount(booking.getTotalAmount().subtract(newTotalPaid));
            if (booking.getBookingStatus() == BookingStatus.PENDING) {
                booking.setBookingStatus(BookingStatus.CONFIRMED);
            }
            bookingRepository.save(booking);

            try {
                emailService.sendPaymentConfirmationEmail(payment);
            } catch (Exception ex) {
                log.warn("Payment email send failed for transaction {}", transactionId, ex);
            }

            return mapToResponse(payment);
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to verify Razorpay payment for booking {}", booking.getBookingReference(), e);
            throw new PaymentException("Razorpay payment verify karatanna error aala: " + e.getMessage());
        }
    }

    private Booking validateBookingAndAmount(Long bookingId, BigDecimal amount) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new PaymentException("Cancelled booking sathi payment gheta yet nahi.");
        }

        if (booking.getBookingStatus() == BookingStatus.CHECKED_OUT) {
            throw new PaymentException("Completed booking sathi payment gheta yet nahi.");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentException("Payment amount 0 peksha motha pahije.");
        }

        // Use balanceAmount from booking entity (authoritative, updated after each payment)
        // Fall back to totalAmount if balanceAmount is null (first payment)
        BigDecimal remaining = booking.getBalanceAmount() != null
                ? booking.getBalanceAmount()
                : booking.getTotalAmount();

        // Round both to 2 decimal places to avoid BigDecimal precision mismatches
        BigDecimal amountRounded   = amount.setScale(2, RoundingMode.HALF_UP);
        BigDecimal remainingRounded = remaining.setScale(2, RoundingMode.HALF_UP);

        if (amountRounded.compareTo(remainingRounded) > 0) {
            throw new PaymentException(
                    "Payment amount ₹" + amountRounded + " remaining balance ₹" + remainingRounded + " peksha jast ahe.");
        }

        return booking;
    }

    private void validateKeysConfigured() {
        if (razorpayKeyId == null || razorpayKeyId.isBlank() || razorpayKeySecret == null || razorpayKeySecret.isBlank()) {
            throw new PaymentException("Razorpay keys configure kara (application.properties kiwa environment variables madhye).");
        }
    }

    private long toPaise(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
    }

    private String buildReceipt(Booking booking) {
        String receipt = "booking_" + booking.getId() + "_" + System.currentTimeMillis();
        return receipt.length() > 40 ? receipt.substring(0, 40) : receipt;
    }

    private String buildNotes(RazorpayVerifyPaymentRequest request) {
        String base = "Razorpay payment | orderId=" + request.getRazorpayOrderId()
                + " | paymentId=" + request.getRazorpayPaymentId();
        if (request.getNotes() != null && !request.getNotes().isBlank()) {
            return base + " | " + request.getNotes();
        }
        return base;
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .transactionId(payment.getTransactionId())
                .bookingId(payment.getBooking().getId())
                .bookingReference(payment.getBooking().getBookingReference())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .paymentType(payment.getPaymentType())
                .paymentDate(payment.getPaymentDate())
                .gatewayReference(payment.getGatewayReference())
                .failureReason(payment.getFailureReason())
                .notes(payment.getNotes())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}