package com.hlt.usermanagement.configuration;

import jakarta.annotation.PostConstruct;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;

@Component
public class FlywayTenantMigrationService {

    private final TenantDataSourceRegistry tenantRegistry;
    private final FlywayProperties flywayProperties;

    public FlywayTenantMigrationService(TenantDataSourceRegistry tenantRegistry,
                                        FlywayProperties flywayProperties) {
        this.tenantRegistry = tenantRegistry;
        this.flywayProperties = flywayProperties;
    }

    @PostConstruct
    public void migrateAllTenants() {
        Map<String, DataSource> tenants = tenantRegistry.getTenantDataSources();

        tenants.forEach((tenantId, dataSource) -> {
            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .locations(flywayProperties.getLocations().toArray(new String[0]))
                    .baselineOnMigrate(true)
                    .load();

            // Repair any failed migrations before proceeding to migrate, so the app can start cleanly
            flyway.repair();
            flyway.migrate();
           
        });
    }

}
