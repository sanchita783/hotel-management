package com.example.repository;

import com.example.entity.BookingStatus;
import com.example.entity.Room;
import com.example.entity.RoomStatus;
import com.example.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findByRoomNumber(String roomNumber);

    boolean existsByRoomNumber(String roomNumber);

    List<Room> findByRoomStatus(RoomStatus status);

    List<Room> findByRoomType(RoomType type);

    List<Room> findByIsActive(Boolean isActive);

    List<Room> findByCapacityGreaterThanEqual(Integer capacity);

    List<Room> findByPricePerNightBetween(BigDecimal minPrice, BigDecimal maxPrice);

    @Query("SELECT COUNT(r) FROM Room r WHERE r.roomStatus = :status AND r.isActive = true")
    Long countByRoomStatus(@Param("status") RoomStatus status);

    @Query("""
        SELECT r FROM Room r
        WHERE r.isActive = true
        AND r.roomStatus = com.example.entity.RoomStatus.AVAILABLE
        AND r.capacity >= :guests
        AND r.id NOT IN (
            SELECT b.room.id FROM Booking b
            WHERE b.bookingStatus NOT IN (
                com.example.entity.BookingStatus.CANCELLED,
                com.example.entity.BookingStatus.CHECKED_OUT
            )
            AND b.checkInDate < :checkOut
            AND b.checkOutDate > :checkIn
        )
    """)
    List<Room> findAvailableRooms(
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("guests") Integer guests
    );

    @Query("""
        SELECT r FROM Room r
        WHERE r.isActive = true
        AND r.roomStatus = com.example.entity.RoomStatus.AVAILABLE
        AND r.capacity >= :guests
        AND r.roomType = :roomType
        AND r.id NOT IN (
            SELECT b.room.id FROM Booking b
            WHERE b.bookingStatus NOT IN (
                com.example.entity.BookingStatus.CANCELLED,
                com.example.entity.BookingStatus.CHECKED_OUT
            )
            AND b.checkInDate < :checkOut
            AND b.checkOutDate > :checkIn
        )
    """)
    List<Room> findAvailableRoomsByType(
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("guests") Integer guests,
            @Param("roomType") RoomType roomType
    );

    @Query("""
        SELECT CASE WHEN COUNT(b) = 0 THEN true ELSE false END
        FROM Booking b
        WHERE b.room.id = :roomId
        AND b.bookingStatus NOT IN (
            com.example.entity.BookingStatus.CANCELLED,
            com.example.entity.BookingStatus.CHECKED_OUT
        )
        AND b.checkInDate < :checkOut
        AND b.checkOutDate > :checkIn
    """)
    boolean isRoomAvailable(
            @Param("roomId") Long roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );
}
