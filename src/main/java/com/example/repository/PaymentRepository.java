package com.example.repository;

import com.example.entity.Payment;
import com.example.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByTransactionId(String transactionId);

    boolean existsByTransactionId(String transactionId);

    boolean existsByGatewayReference(String gatewayReference);

    List<Payment> findByBookingId(Long bookingId);

    List<Payment> findByPaymentStatus(PaymentStatus status);

    @Query("""
        SELECT SUM(p.amount) FROM Payment p
        WHERE p.booking.id = :bookingId
        AND p.paymentStatus = com.example.entity.PaymentStatus.COMPLETED
    """)
    BigDecimal getTotalPaidByBooking(@Param("bookingId") Long bookingId);

    @Query("SELECT p FROM Payment p WHERE p.booking.user.id = :userId ORDER BY p.createdAt DESC")
    List<Payment> findByUserId(@Param("userId") Long userId);
}
