package com.hlt.usermanagement.configuration;

import com.hlt.auth.TenantContextHolder;
import com.hlt.usermanagement.utils.TenantConstants;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class MultiTenantRoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        String tenant = TenantContextHolder.getTenant();
        return (tenant != null) ? tenant : TenantConstants.DEFAULT_TENANT;
    }

}
