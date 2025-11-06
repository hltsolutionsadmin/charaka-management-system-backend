package com.hlt.multitenancy;

import org.flywaydb.core.Flyway;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MultiTenantFlyway {

    private final TenantsProperties tenantsProperties;

    public MultiTenantFlyway(TenantsProperties tenantsProperties) {
        this.tenantsProperties = tenantsProperties;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void migrateAllTenants() {
        for (TenantsProperties.TenantDataSourceProps p : tenantsProperties.getDatasources()) {
            Flyway flyway = Flyway.configure()
                    .dataSource(p.getUrl(), p.getUsername(), p.getPassword())
                    .locations("classpath:db/migration")
                    .baselineOnMigrate(true)
                    .load();
            flyway.migrate();
        }
    }
}
