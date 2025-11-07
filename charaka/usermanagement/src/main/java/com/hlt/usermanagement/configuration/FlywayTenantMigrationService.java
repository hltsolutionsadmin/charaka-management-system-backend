package com.hlt.usermanagement.configuration;

import jakarta.annotation.PostConstruct;
import org.flywaydb.core.Flyway;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Optional;

@Component
public class FlywayTenantMigrationService {

    private final TenantDataSourceRegistry tenantRegistry;
    private final Environment environment;

    public FlywayTenantMigrationService(TenantDataSourceRegistry tenantRegistry,
                                        Environment environment) {
        this.tenantRegistry = tenantRegistry;
        this.environment = environment;
    }

    @PostConstruct
    public void migrateAllTenants() {
        Map<String, DataSource> tenants = tenantRegistry.getTenantDataSources();
        // Resolve migration locations; fallback to default if property missing
        String property = environment.getProperty("spring.flyway.locations");
        String[] locations = Optional.ofNullable(property)
                .map(p -> p.split(","))
                .orElse(new String[]{"classpath:db/migration"});

        tenants.forEach((tenantId, dataSource) -> {
            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .locations(locations)
                    .baselineOnMigrate(true)
                    .load();

            // Repair any failed migrations before proceeding to migrate, so the app can start cleanly
            flyway.repair();
            flyway.migrate();
           
        });
    }

}
