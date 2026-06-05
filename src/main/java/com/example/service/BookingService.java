package com.example.service;

import com.example.dto.request.BookingRequest;
import com.example.dto.response.BookingResponse;
import com.example.entity.BookingStatus;

import java.util.List;

public interface BookingService {

    BookingResponse createBooking(BookingRequest request);

    BookingResponse getBookingById(Long bookingId);

    BookingResponse getBookingByReference(String bookingReference);

    List<BookingResponse> getBookingsByUser(Long userId);

    List<BookingResponse> getCurrentUserBookings();

    List<BookingResponse> getAllBookings();

    List<BookingResponse> getBookingsByStatus(BookingStatus status);

    BookingResponse cancelBooking(Long bookingId, String reason);

    BookingResponse checkIn(Long bookingId);

    BookingResponse checkOut(Long bookingId);

    BookingResponse confirmBooking(Long bookingId);
}
