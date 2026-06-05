package com.example.dto.response;

import com.example.entity.EnquiryStatus;
import com.example.entity.EnquiryType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnquiryResponse {

    private Long id;
    private Long userId;
    private String guestName;
    private String guestEmail;
    private String guestPhone;
    private String subject;
    private String message;
    private String response;
    private EnquiryStatus enquiryStatus;
    private EnquiryType enquiryType;
    private LocalDateTime respondedAt;
    private String respondedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
