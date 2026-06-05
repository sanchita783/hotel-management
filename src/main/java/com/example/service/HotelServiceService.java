package com.example.service;

import com.example.dto.request.HotelServiceRequest;
import com.example.dto.response.HotelServiceResponse;
import com.example.entity.ServiceStatus;
import com.example.entity.ServiceType;

import java.math.BigDecimal;
import java.util.List;

public interface HotelServiceService {

    HotelServiceResponse requestService(HotelServiceRequest request);

    HotelServiceResponse getServiceById(Long serviceId);

    List<HotelServiceResponse> getServicesByBooking(Long bookingId);

    List<HotelServiceResponse> getServicesByCurrentUser();

    List<HotelServiceResponse> getAllServices();

    List<HotelServiceResponse> getServicesByStatus(ServiceStatus status);

    List<HotelServiceResponse> getServicesByType(ServiceType serviceType);

    HotelServiceResponse updateServiceStatus(Long serviceId, ServiceStatus status, String staffNotes);

    HotelServiceResponse updateServiceAmount(Long serviceId, BigDecimal amount);

    void cancelService(Long serviceId);
}
