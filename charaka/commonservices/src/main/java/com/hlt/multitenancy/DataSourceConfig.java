package com.hlt.multitenancy;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskDecorator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import jakarta.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
@EnableConfigurationProperties(TenantsProperties.class)
public class DataSourceConfig {

    @Bean
    public TenantRegistry tenantRegistry(TenantsProperties tenantsProperties) {
        return new TenantRegistry(tenantsProperties);
    }

    @Bean
    @Primary
    public DataSource dataSource(TenantsProperties props) {
        Map<Object, Object> targets = new HashMap<>();
        DataSource defaultDs = null;

        for (TenantsProperties.TenantDataSourceProps p : props.getDatasources()) {
            HikariDataSource ds = new HikariDataSource();
            ds.setJdbcUrl(p.getUrl());
            ds.setUsername(p.getUsername());
            ds.setPassword(p.getPassword());
            ds.setDriverClassName(p.getDriverClassName());
            targets.put(p.getTenantId(), ds);
            if (p.getTenantId().equals(props.getDefaultTenant())) {
                defaultDs = ds;
            }
        }

        if (defaultDs == null && !targets.isEmpty()) {
            // pick any as default if not explicitly set
            defaultDs = (DataSource) targets.values().iterator().next();
        }

        TenantRoutingDataSource routing = new TenantRoutingDataSource();
        // also expose a synthetic "default" key used by routing fallback
        if (defaultDs != null) {
            targets.put("default", defaultDs);
            routing.setDefaultTargetDataSource(defaultDs);
        }
        routing.setTargetDataSources(targets);
        routing.afterPropertiesSet();
        return routing;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        // IMPORTANT: services using this module should configure packagesToScan if needed via properties
        emf.setPackagesToScan("com.hlt");
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        emf.setJpaVendorAdapter(vendorAdapter);
        Properties jpaProps = new Properties();
        jpaProps.put("hibernate.hbm2ddl.auto", "none");
        emf.setJpaProperties(jpaProps);
        return emf;
    }

    @Bean
    public JpaTransactionManager transactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    @Bean
    public TaskDecorator tenantTaskDecorator() {
        return runnable -> {
            String tenant = TenantContext.getTenant();
            return () -> {
                try {
                    TenantContext.setTenant(tenant);
                    runnable.run();
                } finally {
                    TenantContext.clear();
                }
            };
        };
    }
}
