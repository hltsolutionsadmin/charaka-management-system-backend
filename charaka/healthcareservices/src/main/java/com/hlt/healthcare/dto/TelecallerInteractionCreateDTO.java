package com.hlt.healthcare.dto;

import com.hlt.healthcare.dto.enums.InteractionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelecallerInteractionCreateDTO {

    private Long telecallerUserId;

    @NotBlank
    private String mobileNumber;

    private String callerName;

    private String callerEmail;

    @NotNull
    private Long telecallerId;

    @NotNull
    private Long businessId;

    private String notes;

    private InteractionType interactionType;



}
