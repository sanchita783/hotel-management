package com.example.repository;

import com.example.entity.HotelService;
import com.example.entity.ServiceStatus;
import com.example.entity.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelServiceRepository extends JpaRepository<HotelService, Long> {

    List<HotelService> findByBookingId(Long bookingId);

    List<HotelService> findByServiceStatus(ServiceStatus status);

    List<HotelService> findByServiceType(ServiceType type);

    @Query("SELECT hs FROM HotelService hs WHERE hs.booking.user.id = :userId ORDER BY hs.createdAt DESC")
    List<HotelService> findByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT COUNT(hs) FROM HotelService hs
        WHERE hs.serviceStatus IN (
            com.example.entity.ServiceStatus.REQUESTED,
            com.example.entity.ServiceStatus.IN_PROGRESS
        )
    """)
    Long countPendingServices();
}
