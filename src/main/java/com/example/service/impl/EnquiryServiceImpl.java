package com.example.service.impl;

import com.example.dto.request.EnquiryRequest;
import com.example.dto.request.EnquiryResponseRequest;
import com.example.dto.response.EnquiryResponse;
import com.example.entity.Enquiry;
import com.example.entity.EnquiryStatus;
import com.example.entity.User;
import com.example.exception.ResourceNotFoundException;
import com.example.exception.UnauthorizedAccessException;
import com.example.repository.EnquiryRepository;
import com.example.service.EmailService;
import com.example.service.EnquiryService;
import com.example.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EnquiryServiceImpl implements EnquiryService {

    private final EnquiryRepository enquiryRepository;
    private final EmailService emailService;

    @Override
    public EnquiryResponse submitEnquiry(EnquiryRequest request) {
        Enquiry.EnquiryBuilder builder = Enquiry.builder()
                .subject(request.getSubject())
                .message(request.getMessage())
                .enquiryType(request.getEnquiryType())
                .enquiryStatus(EnquiryStatus.OPEN);

        // If authenticated user, link them; otherwise use guest info
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() &&
                    !"anonymousUser".equals(auth.getPrincipal())) {
                User user = SecurityUtils.getCurrentUser();
                builder.user(user)
                        .guestName(user.getFullName())
                        .guestEmail(user.getEmail())
                        .guestPhone(user.getPhone());
            } else {
                builder.guestName(request.getGuestName())
                        .guestEmail(request.getGuestEmail())
                        .guestPhone(request.getGuestPhone());
            }
        } catch (Exception e) {
            builder.guestName(request.getGuestName())
                    .guestEmail(request.getGuestEmail())
                    .guestPhone(request.getGuestPhone());
        }

        Enquiry enquiry = enquiryRepository.save(builder.build());
        log.info("Enquiry submitted: {} - {}", enquiry.getId(), enquiry.getSubject());

        // Send acknowledgement email
        String emailAddress = enquiry.getGuestEmail();
        String guestName = enquiry.getGuestName() != null ? enquiry.getGuestName() : "Guest";
        if (emailAddress != null && !emailAddress.isBlank()) {
            try {
                emailService.sendEnquiryAcknowledgementEmail(emailAddress, guestName,
                        enquiry.getSubject());
            } catch (Exception e) {
                log.error("Failed to send enquiry acknowledgement email: {}", e.getMessage());
            }
        }

        return mapToResponse(enquiry);
    }

    @Override
    @Transactional(readOnly = true)
    public EnquiryResponse getEnquiryById(Long enquiryId) {
        Enquiry enquiry = enquiryRepository.findById(enquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Enquiry", "id", enquiryId));
        checkEnquiryAccess(enquiry);
        return mapToResponse(enquiry);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnquiryResponse> getEnquiriesByCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        return enquiryRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnquiryResponse> getAllEnquiries() {
        return enquiryRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnquiryResponse> getEnquiriesByStatus(EnquiryStatus status) {
        return enquiryRepository.findByEnquiryStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public EnquiryResponse respondToEnquiry(Long enquiryId, EnquiryResponseRequest request) {
        Enquiry enquiry = enquiryRepository.findById(enquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Enquiry", "id", enquiryId));

        enquiry.setResponse(request.getResponse());
        enquiry.setEnquiryStatus(EnquiryStatus.RESOLVED);
        enquiry.setRespondedAt(LocalDateTime.now());
        enquiry.setRespondedBy(SecurityUtils.getCurrentUserEmail());

        enquiry = enquiryRepository.save(enquiry);
        log.info("Enquiry {} responded by {}", enquiryId, SecurityUtils.getCurrentUserEmail());

        // Send response email to guest/user
        String emailAddress = enquiry.getGuestEmail();
        String guestName = enquiry.getGuestName() != null ? enquiry.getGuestName() : "Guest";
        if (emailAddress != null && !emailAddress.isBlank()) {
            try {
                emailService.sendEnquiryResponseEmail(emailAddress, guestName,
                        enquiry.getSubject(), request.getResponse());
            } catch (Exception e) {
                log.error("Failed to send enquiry response email: {}", e.getMessage());
            }
        }

        return mapToResponse(enquiry);
    }

    @Override
    public EnquiryResponse updateEnquiryStatus(Long enquiryId, EnquiryStatus status) {
        Enquiry enquiry = enquiryRepository.findById(enquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Enquiry", "id", enquiryId));
        enquiry.setEnquiryStatus(status);
        enquiry = enquiryRepository.save(enquiry);
        log.info("Enquiry {} status updated to {}", enquiryId, status);
        return mapToResponse(enquiry);
    }

    private void checkEnquiryAccess(Enquiry enquiry) {
        if (!SecurityUtils.isAdmin()) {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            if (enquiry.getUser() == null || !enquiry.getUser().getId().equals(currentUserId)) {
                throw new UnauthorizedAccessException("You don't have access to this enquiry");
            }
        }
    }

    private EnquiryResponse mapToResponse(Enquiry enquiry) {
        return EnquiryResponse.builder()
                .id(enquiry.getId())
                .userId(enquiry.getUser() != null ? enquiry.getUser().getId() : null)
                .guestName(enquiry.getGuestName())
                .guestEmail(enquiry.getGuestEmail())
                .guestPhone(enquiry.getGuestPhone())
                .subject(enquiry.getSubject())
                .message(enquiry.getMessage())
                .response(enquiry.getResponse())
                .enquiryStatus(enquiry.getEnquiryStatus())
                .enquiryType(enquiry.getEnquiryType())
                .respondedAt(enquiry.getRespondedAt())
                .respondedBy(enquiry.getRespondedBy())
                .createdAt(enquiry.getCreatedAt())
                .updatedAt(enquiry.getUpdatedAt())
                .build();
    }
}
