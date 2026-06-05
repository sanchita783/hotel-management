package com.example.repository;

import com.example.entity.Booking;
import com.example.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingReference(String bookingReference);

    boolean existsByBookingReference(String bookingReference);

    List<Booking> findByUserId(Long userId);

    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Booking> findByRoomId(Long roomId);

    List<Booking> findByBookingStatus(BookingStatus status);

    List<Booking> findByUserIdAndBookingStatus(Long userId, BookingStatus status);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.bookingStatus = :status")
    Long countByStatus(@Param("status") BookingStatus status);

    @Query("""
        SELECT COUNT(b) FROM Booking b
        WHERE b.bookingStatus NOT IN (
            com.example.entity.BookingStatus.CANCELLED,
            com.example.entity.BookingStatus.CHECKED_OUT
        )
    """)
    Long countActiveBookings();

    @Query("""
        SELECT SUM(p.amount) FROM Payment p
        WHERE p.paymentStatus = com.example.entity.PaymentStatus.COMPLETED
    """)
    BigDecimal getTotalRevenue();

    @Query("""
        SELECT SUM(p.amount) FROM Payment p
        WHERE p.paymentStatus = com.example.entity.PaymentStatus.COMPLETED
        AND p.paymentDate >= :startDate AND p.paymentDate <= :endDate
    """)
    BigDecimal getRevenueByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("""
        SELECT b FROM Booking b
        WHERE b.checkInDate = :date
        AND b.bookingStatus = com.example.entity.BookingStatus.CONFIRMED
    """)
    List<Booking> findTodayCheckIns(@Param("date") LocalDate date);

    @Query("""
        SELECT b FROM Booking b
        WHERE b.checkOutDate = :date
        AND b.bookingStatus = com.example.entity.BookingStatus.CHECKED_IN
    """)
    List<Booking> findTodayCheckOuts(@Param("date") LocalDate date);
}
