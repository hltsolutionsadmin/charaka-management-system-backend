package com.juvarya.user.access.mgmt.services;

import com.juvarya.user.access.mgmt.dto.ApiKeyDTO;
import com.juvarya.user.access.mgmt.dto.ApiKeyValidationDTO;

public interface ApiKeyService {
    ApiKeyDTO generateApiKeyForUser(Long userId);
    ApiKeyValidationDTO validateApiKey(String rawKey);
}
