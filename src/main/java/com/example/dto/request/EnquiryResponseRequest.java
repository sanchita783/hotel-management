package com.example.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnquiryResponseRequest {

    @NotBlank(message = "Response message is required")
    private String response;
}
