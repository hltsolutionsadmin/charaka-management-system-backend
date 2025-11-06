package com.hlt.multitenancy;

import com.hlt.auth.exception.handling.ErrorCode;
import com.hlt.auth.exception.handling.HltCustomerException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class TenantResolver {

    public static final String HEADER_TENANT_ID = "X-Tenant-Id";

    private final TenantRegistry tenantRegistry;

    public TenantResolver(TenantRegistry tenantRegistry) {
        this.tenantRegistry = tenantRegistry;
    }

    public String resolve(HttpServletRequest request) {
        String tenant = request.getHeader(HEADER_TENANT_ID);
        if (tenant == null || tenant.isBlank()) {
            // fallback strategies can be added here (domain, param)
            throw new HltCustomerException(ErrorCode.UNAUTHORIZED);
        }
        if (!tenantRegistry.isKnown(tenant)) {
            throw new HltCustomerException(ErrorCode.UNAUTHORIZED);
        }
        return tenant;
    }
}
