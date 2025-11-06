package com.hlt.multitenancy;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class TenantRoutingDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        String tenant = TenantContext.getTenant();
        return (tenant == null || tenant.isBlank()) ? "default" : tenant;
    }
}
