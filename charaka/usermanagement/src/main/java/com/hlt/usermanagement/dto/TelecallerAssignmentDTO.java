package com.hlt.usermanagement.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TelecallerAssignmentDTO {

    @NotNull(message = "Telecaller userId is required")
    private Long telecallerUserId;

    @NotNull(message = "Hospital (business) ID is required")
    private Long businessId;

}
