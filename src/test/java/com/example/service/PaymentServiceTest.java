package com.example.service;

import com.example.dto.request.PaymentRequest;
import com.example.dto.response.PaymentResponse;
import com.example.entity.*;
import com.example.exception.PaymentException;
import com.example.exception.ResourceNotFoundException;
import com.example.repository.BookingRepository;
import com.example.repository.PaymentRepository;
import com.example.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Payment Service Tests")
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private EmailService emailService;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private User testUser;
    private Room testRoom;
    private Booking testBooking;
    private Payment testPayment;
    private PaymentRequest paymentRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .role(Role.CUSTOMER)
                .isActive(true)
                .build();

        testRoom = Room.builder()
                .id(1L)
                .roomNumber("101")
                .roomType(RoomType.DOUBLE)
                .pricePerNight(new BigDecimal("2500.00"))
                .roomStatus(RoomStatus.AVAILABLE)
                .isActive(true)
                .build();

        testBooking = Booking.builder()
                .id(1L)
                .bookingReference("BK20240101ABCDEF")
                .user(testUser)
                .room(testRoom)
                .checkInDate(LocalDate.now().plusDays(2))
                .checkOutDate(LocalDate.now().plusDays(4))
                .numberOfGuests(2)
                .totalAmount(new BigDecimal("5000.00"))
                .advancePayment(BigDecimal.ZERO)
                .balanceAmount(new BigDecimal("5000.00"))
                .bookingStatus(BookingStatus.PENDING)
                .build();

        testPayment = Payment.builder()
                .id(1L)
                .transactionId("TXN20240101ABCDEFGH")
                .booking(testBooking)
                .amount(new BigDecimal("5000.00"))
                .paymentMethod(PaymentMethod.UPI)
                .paymentStatus(PaymentStatus.COMPLETED)
                .paymentType(PaymentType.FULL_PAYMENT)
                .paymentDate(LocalDateTime.now())
                .build();

        paymentRequest = PaymentRequest.builder()
                .bookingId(1L)
                .amount(new BigDecimal("5000.00"))
                .paymentMethod(PaymentMethod.UPI)
                .paymentType(PaymentType.FULL_PAYMENT)
                .build();

        SecurityContextHolder.setContext(securityContext);
        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.getPrincipal()).willReturn(testUser);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ─── processPayment ───────────────────────────────────────────────────────

    @Test
    @DisplayName("processPayment - should succeed for valid booking")
    void processPayment_ShouldSucceed_WhenBookingActive() {
        given(bookingRepository.findById(1L)).willReturn(Optional.of(testBooking));
        given(paymentRepository.getTotalPaidByBooking(1L)).willReturn(BigDecimal.ZERO);
        given(paymentRepository.existsByTransactionId(anyString())).willReturn(false);
        given(paymentRepository.save(any(Payment.class))).willReturn(testPayment);
        given(bookingRepository.save(any(Booking.class))).willReturn(testBooking);
        doNothing().when(emailService).sendPaymentConfirmationEmail(any());

        PaymentResponse response = paymentService.processPayment(paymentRequest);

        assertThat(response).isNotNull();
        assertThat(response.getTransactionId()).isEqualTo("TXN20240101ABCDEFGH");
        assertThat(response.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        then(paymentRepository).should().save(any(Payment.class));
    }

    @Test
    @DisplayName("processPayment - should throw for cancelled booking")
    void processPayment_ShouldThrow_WhenBookingCancelled() {
        testBooking.setBookingStatus(BookingStatus.CANCELLED);
        given(bookingRepository.findById(1L)).willReturn(Optional.of(testBooking));

        assertThatThrownBy(() -> paymentService.processPayment(paymentRequest))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("cancelled");
    }

    @Test
    @DisplayName("processPayment - should throw when amount exceeds balance")
    void processPayment_ShouldThrow_WhenAmountExceedsBalance() {
        paymentRequest.setAmount(new BigDecimal("9999.00"));
        given(bookingRepository.findById(1L)).willReturn(Optional.of(testBooking));
        given(paymentRepository.getTotalPaidByBooking(1L)).willReturn(BigDecimal.ZERO);

        assertThatThrownBy(() -> paymentService.processPayment(paymentRequest))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("balance");
    }

    @Test
    @DisplayName("processPayment - auto-confirms PENDING booking on payment")
    void processPayment_ShouldConfirmBooking_WhenPending() {
        given(bookingRepository.findById(1L)).willReturn(Optional.of(testBooking));
        given(paymentRepository.getTotalPaidByBooking(1L)).willReturn(BigDecimal.ZERO);
        given(paymentRepository.existsByTransactionId(anyString())).willReturn(false);
        given(paymentRepository.save(any(Payment.class))).willReturn(testPayment);
        given(bookingRepository.save(any(Booking.class))).willReturn(testBooking);
        doNothing().when(emailService).sendPaymentConfirmationEmail(any());

        paymentService.processPayment(paymentRequest);

        assertThat(testBooking.getBookingStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    // ─── getPaymentById ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getPaymentById - returns payment when found")
    void getPaymentById_ShouldReturn_WhenFound() {
        given(paymentRepository.findById(1L)).willReturn(Optional.of(testPayment));

        PaymentResponse response = paymentService.getPaymentById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("5000.00"));
    }

    @Test
    @DisplayName("getPaymentById - throws when not found")
    void getPaymentById_ShouldThrow_WhenNotFound() {
        given(paymentRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPaymentById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── getPaymentByTransactionId ────────────────────────────────────────────

    @Test
    @DisplayName("getPaymentByTransactionId - returns payment")
    void getPaymentByTransactionId_ShouldReturn() {
        given(paymentRepository.findByTransactionId("TXN20240101ABCDEFGH"))
                .willReturn(Optional.of(testPayment));

        PaymentResponse response = paymentService.getPaymentByTransactionId("TXN20240101ABCDEFGH");

        assertThat(response.getTransactionId()).isEqualTo("TXN20240101ABCDEFGH");
    }

    // ─── getPaymentsByBooking ─────────────────────────────────────────────────

    @Test
    @DisplayName("getPaymentsByBooking - returns all payments for a booking")
    void getPaymentsByBooking_ShouldReturnList() {
        given(paymentRepository.findByBookingId(1L)).willReturn(List.of(testPayment));

        List<PaymentResponse> payments = paymentService.getPaymentsByBooking(1L);

        assertThat(payments).hasSize(1);
        assertThat(payments.get(0).getBookingReference()).isEqualTo("BK20240101ABCDEF");
    }

    // ─── updatePaymentStatus ──────────────────────────────────────────────────

    @Test
    @DisplayName("updatePaymentStatus - updates status")
    void updatePaymentStatus_ShouldUpdate() {
        given(paymentRepository.findById(1L)).willReturn(Optional.of(testPayment));
        given(paymentRepository.save(any())).willReturn(testPayment);

        paymentService.updatePaymentStatus(1L, PaymentStatus.REFUNDED);

        assertThat(testPayment.getPaymentStatus()).isEqualTo(PaymentStatus.REFUNDED);
    }

    // ─── processRefund ────────────────────────────────────────────────────────

    @Test
    @DisplayName("processRefund - creates refund payment record")
    void processRefund_ShouldCreateRefundRecord() {
        Payment refundPayment = Payment.builder()
                .id(2L)
                .transactionId("TXN-REFUND")
                .booking(testBooking)
                .amount(new BigDecimal("-5000.00"))
                .paymentStatus(PaymentStatus.REFUNDED)
                .paymentType(PaymentType.REFUND)
                .build();

        given(paymentRepository.findById(1L)).willReturn(Optional.of(testPayment));
        given(paymentRepository.save(any(Payment.class))).willReturn(refundPayment, testPayment);

        PaymentResponse response = paymentService.processRefund(1L, "Cancellation refund");

        then(paymentRepository).should(times(2)).save(any(Payment.class));
        assertThat(testPayment.getPaymentStatus()).isEqualTo(PaymentStatus.REFUNDED);
    }

    @Test
    @DisplayName("processRefund - throws when payment not COMPLETED")
    void processRefund_ShouldThrow_WhenNotCompleted() {
        testPayment.setPaymentStatus(PaymentStatus.FAILED);
        given(paymentRepository.findById(1L)).willReturn(Optional.of(testPayment));

        assertThatThrownBy(() -> paymentService.processRefund(1L, "reason"))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("refunded");
    }
}
