package com.juvarya.user.access.mgmt.services.impl;

import com.juvarya.auth.exception.handling.ErrorCode;
import com.juvarya.auth.exception.handling.JuvaryaCustomerException;
import com.juvarya.user.access.mgmt.dto.ApiKeyDTO;
import com.juvarya.user.access.mgmt.dto.ApiKeyValidationDTO;
import com.juvarya.user.access.mgmt.model.ApiKeyModel;
import com.juvarya.user.access.mgmt.model.UserModel;
import com.juvarya.user.access.mgmt.repository.ApiKeyRepository;
import com.juvarya.user.access.mgmt.repository.UserRepository;
import com.juvarya.user.access.mgmt.services.ApiKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApiKeyServiceImpl implements ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ApiKeyDTO generateApiKeyForUser(Long userId) {
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new JuvaryaCustomerException(ErrorCode.USER_NOT_FOUND));

        String rawKey = UUID.randomUUID().toString();
        String hashedKey = passwordEncoder.encode(rawKey);
        String tenantKey = "sandbox-" + UUID.randomUUID().toString().substring(0, 8);
        String tenantDbName = "sandbox_user_" + userId;

        ApiKeyModel model = new ApiKeyModel();
        model.setUser(user);
        model.setHashedKey(hashedKey);
        model.setTenantKey(tenantKey);
        model.setTenantDbName(tenantDbName);
        model.setCreatedAt(LocalDateTime.now());
        model.setActive(true);

        apiKeyRepository.save(model);

        return new ApiKeyDTO(rawKey, tenantKey, tenantDbName, model.getCreatedAt());
    }

    @Override
    public ApiKeyValidationDTO validateApiKey(String rawKey) {
        return apiKeyRepository.findAll().stream()
                .filter(k -> k.isActive() && passwordEncoder.matches(rawKey, k.getHashedKey()))
                .map(k -> new ApiKeyValidationDTO(
                        k.getUser().getId(),
                        k.getTenantDbName(),
                        k.getId(), // Changed: ApiKeyId is Long
                        k.getTenantKey()
                ))
                .findFirst()
                .orElseThrow(() -> new JuvaryaCustomerException(ErrorCode.UNAUTHORIZED_API_KEY));
    }
}
