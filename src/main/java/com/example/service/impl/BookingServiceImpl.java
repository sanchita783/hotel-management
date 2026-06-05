package com.example.service.impl;

import com.example.dto.request.BookingRequest;
import com.example.dto.response.BookingResponse;
import com.example.entity.*;
import com.example.exception.BookingException;
import com.example.exception.ResourceNotFoundException;
import com.example.exception.RoomNotAvailableException;
import com.example.exception.UnauthorizedAccessException;
import com.example.repository.BookingRepository;
import com.example.repository.PaymentRepository;
import com.example.repository.RoomRepository;
import com.example.service.BookingService;
import com.example.service.EmailService;
import com.example.util.BookingReferenceGenerator;
import com.example.util.SecurityUtils;
import com.example.util.TransactionIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final PaymentRepository paymentRepository;
    private final EmailService emailService;

    @Override
    public BookingResponse createBooking(BookingRequest request) {
        User currentUser = SecurityUtils.getCurrentUser();

        // NOTE: Email verification is NOT required for booking.
        // Users can book rooms without verifying their email.
        // Only valid JWT token (login) is needed.

        // Validate dates
        validateBookingDates(request.getCheckInDate(), request.getCheckOutDate());

        // Fetch and validate room
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", request.getRoomId()));

        if (!room.getIsActive() || room.getRoomStatus() == RoomStatus.MAINTENANCE) {
            throw new RoomNotAvailableException("Room " + room.getRoomNumber() + " is not available for booking");
        }

        if (request.getNumberOfGuests() > room.getCapacity()) {
            throw new BookingException("Number of guests (" + request.getNumberOfGuests() +
                    ") exceeds room capacity (" + room.getCapacity() + ")");
        }

        boolean available = roomRepository.isRoomAvailable(
                room.getId(), request.getCheckInDate(), request.getCheckOutDate());
        if (!available) {
            throw new RoomNotAvailableException("Room " + room.getRoomNumber() +
                    " is not available for the selected dates");
        }

        // Generate unique booking reference
        String reference;
        do {
            reference = BookingReferenceGenerator.generate();
        } while (bookingRepository.existsByBookingReference(reference));

        // Build booking with advance payment from request
        BigDecimal advance = request.getAdvancePayment() != null
                ? request.getAdvancePayment() : BigDecimal.ZERO;

        Booking booking = Booking.builder()
                .bookingReference(reference)
                .user(currentUser)
                .room(room)
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .numberOfGuests(request.getNumberOfGuests())
                .advancePayment(advance)
                .specialRequests(request.getSpecialRequests())
                .bookingStatus(BookingStatus.PENDING)
                .build();

        booking.calculateTotalAmount();

        // Validate advance payment
        if (advance.compareTo(booking.getTotalAmount()) > 0) {
            throw new BookingException("Advance payment ₹" + advance +
                    " cannot exceed total amount ₹" + booking.getTotalAmount());
        }

        // Set balance and auto-confirm if full payment
        booking.setBalanceAmount(booking.getTotalAmount().subtract(advance));
        if (advance.compareTo(booking.getTotalAmount()) >= 0) {
            booking.setBookingStatus(BookingStatus.CONFIRMED);
        }

        booking = bookingRepository.save(booking);

        // Save payment record if advance > 0
        if (advance.compareTo(BigDecimal.ZERO) > 0) {
            String txnId;
            do { txnId = TransactionIdGenerator.generate(); }
            while (paymentRepository.existsByTransactionId(txnId));

            Payment payment = Payment.builder()
                    .transactionId(txnId)
                    .booking(booking)
                    .amount(advance)
                    .paymentMethod(request.getPaymentMethod())
                    .paymentStatus(PaymentStatus.COMPLETED)
                    .paymentType(PaymentType.ADVANCE)
                    .paymentDate(LocalDateTime.now())
                    .notes("Advance payment at booking. Method: " + request.getPaymentMethod())
                    .build();
            paymentRepository.save(payment);
            log.info("Advance payment saved: txn={} amount={}", txnId, advance);
        }

        log.info("Booking created: {} for user: {}", booking.getBookingReference(), currentUser.getEmail());

        final Booking finalBooking = booking;
        try {
            emailService.sendBookingConfirmationEmail(finalBooking);
        } catch (Exception e) {
            log.error("Failed to send booking confirmation email: {}", e.getMessage());
        }

        return mapToResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long bookingId) {
        Booking booking = findBookingWithAccessCheck(bookingId);
        return mapToResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingByReference(String bookingReference) {
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "reference", bookingReference));
        checkBookingAccess(booking);
        return mapToResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByUser(Long userId) {
        if (!SecurityUtils.isAdmin() && !SecurityUtils.isCurrentUser(userId)) {
            throw new UnauthorizedAccessException("You can only view your own bookings");
        }
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getCurrentUserBookings() {
        return getBookingsByUser(SecurityUtils.getCurrentUserId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByStatus(BookingStatus status) {
        return bookingRepository.findByBookingStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BookingResponse cancelBooking(Long bookingId, String reason) {
        Booking booking = findBookingWithAccessCheck(bookingId);

        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new BookingException("Booking is already cancelled");
        }
        if (booking.getBookingStatus() == BookingStatus.CHECKED_OUT) {
            throw new BookingException("Cannot cancel a completed booking");
        }
        if (booking.getCheckInDate().isBefore(LocalDate.now())) {
            throw new BookingException("Cannot cancel a booking with a past check-in date");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(reason);
        booking.setCancelledAt(LocalDateTime.now());
        booking = bookingRepository.save(booking);

        log.info("Booking cancelled: {}", booking.getBookingReference());

        final Booking finalBooking = booking;
        try {
            emailService.sendBookingCancellationEmail(finalBooking);
        } catch (Exception e) {
            log.error("Failed to send cancellation email: {}", e.getMessage());
        }

        return mapToResponse(booking);
    }

    @Override
    public BookingResponse checkIn(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new BookingException("Only confirmed bookings can be checked in");
        }

        booking.setBookingStatus(BookingStatus.CHECKED_IN);
        booking.setCheckedInAt(LocalDateTime.now());
        booking.getRoom().setRoomStatus(RoomStatus.OCCUPIED);
        booking = bookingRepository.save(booking);

        log.info("Check-in completed for booking: {}", booking.getBookingReference());

        try {
            emailService.sendCheckInEmail(booking);
        } catch (Exception e) {
            log.error("Failed to send check-in email: {}", e.getMessage());
        }

        return mapToResponse(booking);
    }

    @Override
    public BookingResponse checkOut(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (booking.getBookingStatus() != BookingStatus.CHECKED_IN) {
            throw new BookingException("Only checked-in bookings can be checked out");
        }

        booking.setBookingStatus(BookingStatus.CHECKED_OUT);
        booking.setCheckedOutAt(LocalDateTime.now());
        booking.getRoom().setRoomStatus(RoomStatus.CLEANING);
        booking = bookingRepository.save(booking);

        log.info("Check-out completed for booking: {}", booking.getBookingReference());

        try {
            emailService.sendCheckOutEmail(booking);
        } catch (Exception e) {
            log.error("Failed to send check-out email: {}", e.getMessage());
        }

        return mapToResponse(booking);
    }

    @Override
    public BookingResponse confirmBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (booking.getBookingStatus() != BookingStatus.PENDING) {
            throw new BookingException("Only pending bookings can be confirmed");
        }

        booking.setBookingStatus(BookingStatus.CONFIRMED);
        booking = bookingRepository.save(booking);
        log.info("Booking confirmed: {}", booking.getBookingReference());
        return mapToResponse(booking);
    }

    private Booking findBookingWithAccessCheck(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));
        checkBookingAccess(booking);
        return booking;
    }

    private void checkBookingAccess(Booking booking) {
        if (!SecurityUtils.isAdmin() && !booking.getUser().getId().equals(SecurityUtils.getCurrentUserId())) {
            throw new UnauthorizedAccessException("You don't have access to this booking");
        }
    }

    private void validateBookingDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn.isBefore(LocalDate.now())) {
            throw new BookingException("Check-in date cannot be in the past");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new BookingException("Check-out date must be after check-in date");
        }
        if (checkOut.isAfter(checkIn.plusDays(30))) {
            throw new BookingException("Booking duration cannot exceed 30 days");
        }
    }

    private BookingResponse mapToResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .bookingReference(booking.getBookingReference())
                .userId(booking.getUser().getId())
                .guestName(booking.getUser().getFullName())
                .guestEmail(booking.getUser().getEmail())
                .roomId(booking.getRoom().getId())
                .roomNumber(booking.getRoom().getRoomNumber())
                .roomType(booking.getRoom().getRoomType().name())
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .numberOfNights(booking.getNumberOfNights())
                .numberOfGuests(booking.getNumberOfGuests())
                .pricePerNight(booking.getRoom().getPricePerNight())
                .totalAmount(booking.getTotalAmount())
                .advancePayment(booking.getAdvancePayment())
                .balanceAmount(booking.getBalanceAmount())
                .bookingStatus(booking.getBookingStatus())
                .specialRequests(booking.getSpecialRequests())
                .cancellationReason(booking.getCancellationReason())
                .cancelledAt(booking.getCancelledAt())
                .checkedInAt(booking.getCheckedInAt())
                .checkedOutAt(booking.getCheckedOutAt())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}