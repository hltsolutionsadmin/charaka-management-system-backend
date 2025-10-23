package com.hlt.auth;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class TenantFilter implements Filter {

    private static final String TENANT_HEADER = "X-Tenant-ID";
    public static final String DEFAULT_TENANT = "juvaryahmc";
    public static final String HMC_TENANT = "juvaryahospital";
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {


        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String tenantId = httpRequest.getHeader(TENANT_HEADER);

        if (tenantId == null || tenantId.isEmpty()) {
            tenantId = DEFAULT_TENANT;
        } else if (tenantId.equalsIgnoreCase("HMC")) {
            tenantId = HMC_TENANT; // "juvaryahmc"
        }

        TenantContextHolder.setTenant(tenantId);
        try {
            chain.doFilter(request, response);
        } finally {
            TenantContextHolder.clear();
        }
    }
}
