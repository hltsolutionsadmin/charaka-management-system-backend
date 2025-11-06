package com.hlt.multitenancy;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignTenantConfig {

    @Bean
    public RequestInterceptor tenantRequestInterceptor() {
        return template -> {
            String tenant = TenantContext.getTenant();
            if (tenant != null && !tenant.isBlank()) {
                template.header(TenantResolver.HEADER_TENANT_ID, tenant);
            }
        };
    }
}
