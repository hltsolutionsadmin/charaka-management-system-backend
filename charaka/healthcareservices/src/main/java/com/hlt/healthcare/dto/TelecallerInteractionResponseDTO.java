package com.hlt.healthcare.dto;

import com.hlt.healthcare.dto.enums.InteractionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelecallerInteractionResponseDTO {

    private Long id;

    private Long callerUserId;

    private String callerName;
    private String callerEmail;
    private String mobileNumber;

    private Long telecallerId;
    private Long businessId;

    private InteractionType interactionType;

    private String notes;
    private LocalDateTime createdAt;
}
