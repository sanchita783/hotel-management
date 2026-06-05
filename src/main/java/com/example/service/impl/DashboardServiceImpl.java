package com.example.service.impl;

import com.example.dto.response.DashboardResponse;
import com.example.entity.BookingStatus;
import com.example.entity.EnquiryStatus;
import com.example.entity.Role;
import com.example.entity.RoomStatus;
import com.example.repository.*;
import com.example.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final EnquiryRepository enquiryRepository;
    private final HotelServiceRepository hotelServiceRepository;

    @Override
    public DashboardResponse getDashboardStats() {
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0);
        LocalDateTime monthEnd   = LocalDateTime.now();

        Long totalUsers     = userRepository.countByRole(Role.CUSTOMER);
        Long totalRooms     = roomRepository.count();
        Long availableRooms = roomRepository.countByRoomStatus(RoomStatus.AVAILABLE);
        Long occupiedRooms  = roomRepository.countByRoomStatus(RoomStatus.OCCUPIED);

        Long totalBookings     = bookingRepository.count();
        Long activeBookings    = bookingRepository.countActiveBookings();
        Long pendingBookings   = bookingRepository.countByStatus(BookingStatus.PENDING);
        Long cancelledBookings = bookingRepository.countByStatus(BookingStatus.CANCELLED);

        BigDecimal totalRevenue   = bookingRepository.getTotalRevenue();
        BigDecimal monthlyRevenue = bookingRepository.getRevenueByDateRange(monthStart, monthEnd);

        Long totalEnquiries = enquiryRepository.count();
        Long openEnquiries  = enquiryRepository.countOpenEnquiries();
        Long pendingServices = hotelServiceRepository.countPendingServices();

        return DashboardResponse.builder()
                .totalUsers(totalUsers)
                .totalRooms(totalRooms)
                .availableRooms(availableRooms)
                .occupiedRooms(occupiedRooms)
                .totalBookings(totalBookings)
                .activeBookings(activeBookings)
                .pendingBookings(pendingBookings)
                .cancelledBookings(cancelledBookings)
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .monthlyRevenue(monthlyRevenue != null ? monthlyRevenue : BigDecimal.ZERO)
                .totalEnquiries(totalEnquiries)
                .openEnquiries(openEnquiries)
                .pendingServices(pendingServices)
                .build();
    }
}
