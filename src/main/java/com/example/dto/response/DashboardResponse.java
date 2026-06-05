package com.example.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {

    private Long totalUsers;
    private Long totalRooms;
    private Long availableRooms;
    private Long occupiedRooms;
    private Long totalBookings;
    private Long activeBookings;
    private Long pendingBookings;
    private Long cancelledBookings;
    private BigDecimal totalRevenue;
    private BigDecimal monthlyRevenue;
    private Long totalEnquiries;
    private Long openEnquiries;
    private Long pendingServices;
}
