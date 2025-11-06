package com.hlt.multitenancy;

import java.util.HashSet;
import java.util.Set;

public class TenantRegistry {
    private final Set<String> knownTenants = new HashSet<>();
    private final String defaultTenant;

    public TenantRegistry(TenantsProperties properties) {
        this.defaultTenant = properties.getDefaultTenant();
        properties.getDatasources().forEach(ds -> knownTenants.add(ds.getTenantId()));
    }

    public boolean isKnown(String tenantId) {
        return tenantId != null && knownTenants.contains(tenantId);
    }

    public String getDefaultTenant() {
        return defaultTenant;
    }
}
