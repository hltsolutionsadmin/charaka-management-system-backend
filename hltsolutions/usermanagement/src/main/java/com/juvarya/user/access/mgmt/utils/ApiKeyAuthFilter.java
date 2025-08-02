package com.juvarya.user.access.mgmt.utils;

import com.juvarya.commonservice.dto.RequestContext;
import com.juvarya.user.access.mgmt.dto.ApiKeyValidationDTO;
import com.juvarya.user.access.mgmt.services.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final ApiKeyService apiKeyService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String rawKey = request.getHeader("X-API-KEY");

        try {
            if (rawKey != null && !rawKey.isBlank()) {
                log.debug("API Key received: {}", rawKey);

                ApiKeyValidationDTO dto = apiKeyService.validateApiKey(rawKey);

                RequestContext.setApiKeyId(Long.valueOf(dto.getApiKeyId()));
                RequestContext.setUserId(dto.getUserId());
                RequestContext.setTenantKey(dto.getTenantKey());
                RequestContext.setTenantDbName(dto.getTenantDbName());

                log.debug("API key validated for sandbox. userId={}, apiKeyId={}, tenantKey={}, tenantDbName={}",
                        dto.getUserId(), dto.getApiKeyId(), dto.getTenantKey(), dto.getTenantDbName());

                // Set security context with userId only
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(dto.getUserId(), null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);

        } finally {
            RequestContext.clear();
            SecurityContextHolder.clearContext();
        }
    }
}
