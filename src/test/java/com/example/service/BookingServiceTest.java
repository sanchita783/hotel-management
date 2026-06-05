package com.example.service;

import com.example.dto.request.BookingRequest;
import com.example.dto.response.BookingResponse;
import com.example.entity.*;
import com.example.exception.BookingException;
import com.example.exception.ResourceNotFoundException;
import com.example.exception.RoomNotAvailableException;
import com.example.repository.BookingRepository;
import com.example.repository.PaymentRepository;
import com.example.repository.RoomRepository;
import com.example.service.impl.BookingServiceImpl;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Booking Service Tests")
class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private EmailService emailService;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User testUser;
    private Room testRoom;
    private Booking testBooking;
    private BookingRequest bookingRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .role(Role.CUSTOMER)
                .isActive(true)
                .build();

        testRoom = Room.builder()
                .id(1L)
                .roomNumber("101")
                .roomType(RoomType.DOUBLE)
                .pricePerNight(new BigDecimal("2500.00"))
                .capacity(2)
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

        bookingRequest = BookingRequest.builder()
                .roomId(1L)
                .checkInDate(LocalDate.now().plusDays(2))
                .checkOutDate(LocalDate.now().plusDays(4))
                .numberOfGuests(2)
                .paymentMethod(PaymentMethod.CASH)
                .advancePayment(BigDecimal.ZERO)
                .build();

        SecurityContextHolder.setContext(securityContext);
        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.getPrincipal()).willReturn(testUser);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should create booking when room is available")
    void createBooking_ShouldSucceed_WhenRoomAvailable() {
        given(roomRepository.findById(1L)).willReturn(Optional.of(testRoom));
        given(roomRepository.isRoomAvailable(1L, bookingRequest.getCheckInDate(),
                bookingRequest.getCheckOutDate())).willReturn(true);
        given(bookingRepository.existsByBookingReference(anyString())).willReturn(false);
        given(bookingRepository.save(any(Booking.class))).willReturn(testBooking);
        doNothing().when(emailService).sendBookingConfirmationEmail(any());

        BookingResponse response = bookingService.createBooking(bookingRequest);

        assertThat(response).isNotNull();
        assertThat(response.getBookingStatus()).isEqualTo(BookingStatus.PENDING);
        then(bookingRepository).should().save(any(Booking.class));
    }

    @Test
    @DisplayName("Should throw RoomNotAvailableException when room is booked")
    void createBooking_ShouldThrow_WhenRoomNotAvailable() {
        given(roomRepository.findById(1L)).willReturn(Optional.of(testRoom));
        given(roomRepository.isRoomAvailable(anyLong(), any(), any())).willReturn(false);

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequest))
                .isInstanceOf(RoomNotAvailableException.class);
    }

    @Test
    @DisplayName("Should throw BookingException when guests exceed room capacity")
    void createBooking_ShouldThrow_WhenGuestsExceedCapacity() {
        bookingRequest.setNumberOfGuests(10);
        given(roomRepository.findById(1L)).willReturn(Optional.of(testRoom));

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequest))
                .isInstanceOf(BookingException.class)
                .hasMessageContaining("capacity");
    }

    @Test
    @DisplayName("Should cancel booking successfully")
    void cancelBooking_ShouldSucceed_WhenBookingIsPending() {
        given(bookingRepository.findById(1L)).willReturn(Optional.of(testBooking));
        given(bookingRepository.save(any(Booking.class))).willReturn(testBooking);
        doNothing().when(emailService).sendBookingCancellationEmail(any());

        BookingResponse response = bookingService.cancelBooking(1L, "Changed plans");

        assertThat(testBooking.getBookingStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(testBooking.getCancellationReason()).isEqualTo("Changed plans");
    }

    @Test
    @DisplayName("Should throw BookingException when cancelling already cancelled booking")
    void cancelBooking_ShouldThrow_WhenAlreadyCancelled() {
        testBooking.setBookingStatus(BookingStatus.CANCELLED);
        given(bookingRepository.findById(1L)).willReturn(Optional.of(testBooking));

        assertThatThrownBy(() -> bookingService.cancelBooking(1L, "reason"))
                .isInstanceOf(BookingException.class)
                .hasMessageContaining("already cancelled");
    }

    @Test
    @DisplayName("Should confirm booking when status is PENDING")
    void confirmBooking_ShouldSucceed_WhenPending() {
        given(bookingRepository.findById(1L)).willReturn(Optional.of(testBooking));
        given(bookingRepository.save(any())).willReturn(testBooking);

        bookingService.confirmBooking(1L);

        assertThat(testBooking.getBookingStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Should check in successfully when booking is CONFIRMED")
    void checkIn_ShouldSucceed_WhenConfirmed() {
        testBooking.setBookingStatus(BookingStatus.CONFIRMED);
        given(bookingRepository.findById(1L)).willReturn(Optional.of(testBooking));
        given(bookingRepository.save(any())).willReturn(testBooking);
        doNothing().when(emailService).sendCheckInEmail(any());

        bookingService.checkIn(1L);

        assertThat(testBooking.getBookingStatus()).isEqualTo(BookingStatus.CHECKED_IN);
        assertThat(testBooking.getRoom().getRoomStatus()).isEqualTo(RoomStatus.OCCUPIED);
    }

    @Test
    @DisplayName("Should checkout successfully when booking is CHECKED_IN")
    void checkOut_ShouldSucceed_WhenCheckedIn() {
        testBooking.setBookingStatus(BookingStatus.CHECKED_IN);
        given(bookingRepository.findById(1L)).willReturn(Optional.of(testBooking));
        given(bookingRepository.save(any())).willReturn(testBooking);
        doNothing().when(emailService).sendCheckOutEmail(any());

        bookingService.checkOut(1L);

        assertThat(testBooking.getBookingStatus()).isEqualTo(BookingStatus.CHECKED_OUT);
        assertThat(testBooking.getRoom().getRoomStatus()).isEqualTo(RoomStatus.CLEANING);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when booking not found")
    void getBookingById_ShouldThrow_WhenNotFound() {
        given(bookingRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBookingById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should return bookings for current user")
    void getCurrentUserBookings_ShouldReturnUserBookings() {
        given(bookingRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .willReturn(List.of(testBooking));

        List<BookingResponse> bookings = bookingService.getCurrentUserBookings();

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getUserId()).isEqualTo(1L);
    }
}
