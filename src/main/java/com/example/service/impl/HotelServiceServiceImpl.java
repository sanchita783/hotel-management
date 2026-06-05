package com.example.service.impl;

import com.example.dto.request.HotelServiceRequest;
import com.example.dto.response.HotelServiceResponse;
import com.example.entity.*;
import com.example.exception.BookingException;
import com.example.exception.ResourceNotFoundException;
import com.example.exception.UnauthorizedAccessException;
import com.example.repository.BookingRepository;
import com.example.repository.HotelServiceRepository;
import com.example.service.HotelServiceService;
import com.example.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class HotelServiceServiceImpl implements HotelServiceService {

    private final HotelServiceRepository hotelServiceRepository;
    private final BookingRepository bookingRepository;

    @Override
    public HotelServiceResponse requestService(HotelServiceRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", request.getBookingId()));

        // Only the booking owner can request services
        if (!SecurityUtils.isAdmin() &&
                !booking.getUser().getId().equals(SecurityUtils.getCurrentUserId())) {
            throw new UnauthorizedAccessException("You can only request services for your own bookings");
        }

        if (booking.getBookingStatus() != BookingStatus.CHECKED_IN &&
                booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new BookingException("Services can only be requested for confirmed or checked-in bookings");
        }

        HotelService service = HotelService.builder()
                .booking(booking)
                .serviceType(request.getServiceType())
                .serviceDescription(request.getServiceDescription())
                .serviceStatus(ServiceStatus.REQUESTED)
                .build();

        service = hotelServiceRepository.save(service);
        log.info("Service requested: {} for booking: {}", request.getServiceType(),
                booking.getBookingReference());
        return mapToResponse(service);
    }

    @Override
    @Transactional(readOnly = true)
    public HotelServiceResponse getServiceById(Long serviceId) {
        HotelService service = hotelServiceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("HotelService", "id", serviceId));
        checkServiceAccess(service);
        return mapToResponse(service);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelServiceResponse> getServicesByBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));
        if (!SecurityUtils.isAdmin() &&
                !booking.getUser().getId().equals(SecurityUtils.getCurrentUserId())) {
            throw new UnauthorizedAccessException("You can only view services for your own bookings");
        }
        return hotelServiceRepository.findByBookingId(bookingId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelServiceResponse> getServicesByCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        return hotelServiceRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelServiceResponse> getAllServices() {
        return hotelServiceRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelServiceResponse> getServicesByStatus(ServiceStatus status) {
        return hotelServiceRepository.findByServiceStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelServiceResponse> getServicesByType(ServiceType serviceType) {
        return hotelServiceRepository.findByServiceType(serviceType).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public HotelServiceResponse updateServiceStatus(Long serviceId, ServiceStatus status,
                                                     String staffNotes) {
        HotelService service = hotelServiceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("HotelService", "id", serviceId));

        service.setServiceStatus(status);
        if (staffNotes != null) service.setStaffNotes(staffNotes);
        if (status == ServiceStatus.COMPLETED) service.setCompletedAt(LocalDateTime.now());

        service = hotelServiceRepository.save(service);
        log.info("Service {} status updated to {}", serviceId, status);
        return mapToResponse(service);
    }

    @Override
    public HotelServiceResponse updateServiceAmount(Long serviceId, BigDecimal amount) {
        HotelService service = hotelServiceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("HotelService", "id", serviceId));
        service.setAmount(amount);
        service = hotelServiceRepository.save(service);
        return mapToResponse(service);
    }

    @Override
    public void cancelService(Long serviceId) {
        HotelService service = hotelServiceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("HotelService", "id", serviceId));
        checkServiceAccess(service);

        if (service.getServiceStatus() == ServiceStatus.COMPLETED) {
            throw new BookingException("Cannot cancel a completed service");
        }
        service.setServiceStatus(ServiceStatus.CANCELLED);
        hotelServiceRepository.save(service);
        log.info("Service {} cancelled", serviceId);
    }

    private void checkServiceAccess(HotelService service) {
        if (!SecurityUtils.isAdmin() &&
                !service.getBooking().getUser().getId().equals(SecurityUtils.getCurrentUserId())) {
            throw new UnauthorizedAccessException("You don't have access to this service");
        }
    }

    private HotelServiceResponse mapToResponse(HotelService service) {
        return HotelServiceResponse.builder()
                .id(service.getId())
                .bookingId(service.getBooking().getId())
                .bookingReference(service.getBooking().getBookingReference())
                .serviceType(service.getServiceType())
                .serviceDescription(service.getServiceDescription())
                .amount(service.getAmount())
                .serviceStatus(service.getServiceStatus())
                .requestedAt(service.getRequestedAt())
                .completedAt(service.getCompletedAt())
                .staffNotes(service.getStaffNotes())
                .createdAt(service.getCreatedAt())
                .build();
    }
}
