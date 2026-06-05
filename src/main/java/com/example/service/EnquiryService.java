package com.example.service;

import com.example.dto.request.EnquiryRequest;
import com.example.dto.request.EnquiryResponseRequest;
import com.example.dto.response.EnquiryResponse;
import com.example.entity.EnquiryStatus;

import java.util.List;

public interface EnquiryService {

    EnquiryResponse submitEnquiry(EnquiryRequest request);

    EnquiryResponse getEnquiryById(Long enquiryId);

    List<EnquiryResponse> getEnquiriesByCurrentUser();

    List<EnquiryResponse> getAllEnquiries();

    List<EnquiryResponse> getEnquiriesByStatus(EnquiryStatus status);

    EnquiryResponse respondToEnquiry(Long enquiryId, EnquiryResponseRequest request);

    EnquiryResponse updateEnquiryStatus(Long enquiryId, EnquiryStatus status);
}
