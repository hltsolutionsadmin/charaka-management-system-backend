package com.hlt.usermanagement.configuration;

import com.hlt.usermanagement.utils.TenantConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class MultiTenantDataSourceConfig {

    private final TenantDataSourceRegistry tenantRegistry;

    public MultiTenantDataSourceConfig(TenantDataSourceRegistry tenantRegistry) {
        this.tenantRegistry = tenantRegistry;
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        Map<String, DataSource> tenantDataSources = tenantRegistry.getTenantDataSources();

        MultiTenantRoutingDataSource routingDataSource = new MultiTenantRoutingDataSource();
        routingDataSource.setTargetDataSources(new HashMap<>(tenantDataSources));
        routingDataSource.setDefaultTargetDataSource(tenantDataSources.get(TenantConstants.DEFAULT_TENANT));
        routingDataSource.afterPropertiesSet();

        return routingDataSource;
    }
}
