package com.juvarya.user.access.mgmt.dto;

import java.time.LocalDateTime;

public record ApiKeyDTO(
        String apiKey,
        String tenantKey,
        String tenantDbName,
        LocalDateTime createdAt
) {}
