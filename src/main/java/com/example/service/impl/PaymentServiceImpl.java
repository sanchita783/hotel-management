package com.example.service.impl;

import com.example.dto.request.PaymentRequest;
import com.example.dto.response.PaymentResponse;
import com.example.entity.*;
import com.example.exception.PaymentException;
import com.example.exception.ResourceNotFoundException;
import com.example.repository.BookingRepository;
import com.example.repository.PaymentRepository;
import com.example.service.EmailService;
import com.example.service.PaymentService;
import com.example.util.SecurityUtils;
import com.example.util.TransactionIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final EmailService emailService;

    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", request.getBookingId()));

        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new PaymentException("Cannot process payment for a cancelled booking");
        }
        if (booking.getBookingStatus() == BookingStatus.CHECKED_OUT) {
            throw new PaymentException("Cannot process payment for a completed booking");
        }

        // Validate amount doesn't exceed balance
        BigDecimal totalPaid = paymentRepository.getTotalPaidByBooking(booking.getId());
        if (totalPaid == null) totalPaid = BigDecimal.ZERO;

        BigDecimal remaining = booking.getTotalAmount().subtract(totalPaid);
        if (request.getAmount().compareTo(remaining) > 0) {
            throw new PaymentException("Payment amount (" + request.getAmount() +
                    ") exceeds balance due (" + remaining + ")");
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
                .gatewayReference(request.getGatewayReference())
                .notes(request.getNotes())
                .build();

        payment = paymentRepository.save(payment);

        // Update booking balance
        BigDecimal newTotalPaid = totalPaid.add(request.getAmount());
        booking.setAdvancePayment(newTotalPaid);
        booking.setBalanceAmount(booking.getTotalAmount().subtract(newTotalPaid));

        // Auto-confirm booking if pending and payment received
        if (booking.getBookingStatus() == BookingStatus.PENDING) {
            booking.setBookingStatus(BookingStatus.CONFIRMED);
        }
        bookingRepository.save(booking);

        log.info("Payment processed: {} for booking: {}", payment.getTransactionId(),
                booking.getBookingReference());

        final Payment finalPayment = payment;
        try {
            emailService.sendPaymentConfirmationEmail(finalPayment);
        } catch (Exception e) {
            log.error("Failed to send payment confirmation email: {}", e.getMessage());
        }

        return mapToResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));
        return mapToResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByTransactionId(String transactionId) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "transactionId", transactionId));
        return mapToResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByBooking(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        return paymentRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByPaymentStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentResponse updatePaymentStatus(Long paymentId, PaymentStatus status) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));
        payment.setPaymentStatus(status);
        payment = paymentRepository.save(payment);
        log.info("Payment {} status updated to {}", payment.getTransactionId(), status);
        return mapToResponse(payment);
    }

    @Override
    public PaymentResponse processRefund(Long paymentId, String reason) {
        Payment original = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        if (original.getPaymentStatus() != PaymentStatus.COMPLETED) {
            throw new PaymentException("Only completed payments can be refunded");
        }

        Payment refund = Payment.builder()
                .transactionId(TransactionIdGenerator.generate())
                .booking(original.getBooking())
                .amount(original.getAmount().negate())
                .paymentMethod(original.getPaymentMethod())
                .paymentStatus(PaymentStatus.REFUNDED)
                .paymentType(PaymentType.REFUND)
                .paymentDate(LocalDateTime.now())
                .notes("Refund for transaction: " + original.getTransactionId() + ". Reason: " + reason)
                .build();

        refund = paymentRepository.save(refund);
        original.setPaymentStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(original);

        log.info("Refund processed: {} for original: {}", refund.getTransactionId(),
                original.getTransactionId());
        return mapToResponse(refund);
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
