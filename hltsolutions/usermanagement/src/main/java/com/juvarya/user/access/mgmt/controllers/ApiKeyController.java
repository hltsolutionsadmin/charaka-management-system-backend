package com.juvarya.user.access.mgmt.controllers;

import com.juvarya.commonservice.dto.StandardResponse;
import com.juvarya.commonservice.user.UserDetailsImpl;
import com.juvarya.user.access.mgmt.dto.ApiKeyDTO;
import com.juvarya.user.access.mgmt.dto.ApiKeyValidationDTO;
import com.juvarya.user.access.mgmt.services.ApiKeyService;
import com.juvarya.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/apikey")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    @PostMapping("/generate")
    public StandardResponse<ApiKeyDTO> generateApiKey() {
        UserDetailsImpl loggedInUser = SecurityUtils.getCurrentUserDetails();
        ApiKeyDTO dto = apiKeyService.generateApiKeyForUser(loggedInUser.getId());
        return StandardResponse.single("API key generated successfully", dto);
    }

    @PostMapping("/validate")
    public StandardResponse<ApiKeyValidationDTO> validateApiKey(@RequestHeader("X-API-KEY") String rawKey) {
        ApiKeyValidationDTO dto = apiKeyService.validateApiKey(rawKey);
        return StandardResponse.single("API key is valid", dto);
    }
}
