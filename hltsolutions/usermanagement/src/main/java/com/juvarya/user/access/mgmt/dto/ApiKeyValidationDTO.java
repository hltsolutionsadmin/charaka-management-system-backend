package com.juvarya.user.access.mgmt.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiKeyValidationDTO {
    private Long userId;
    private String tenantDbName;
    private Long apiKeyId;
    private String tenantKey;
}
