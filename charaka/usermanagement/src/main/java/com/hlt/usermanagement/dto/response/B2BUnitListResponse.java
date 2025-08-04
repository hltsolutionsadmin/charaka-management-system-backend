package com.hlt.usermanagement.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class B2BUnitListResponse {
    private Long id;

    private String businessName;

    private boolean approved;

    private String categoryName;

    private LocalDateTime creationDate;

    private Long userId;

    private boolean enabled;

    private Set<BusinessAttributeResponse> attributes;
}

