package com.hlt.usermanagement.configuration;

import com.hlt.usermanagement.utils.TenantConstants;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class MultiTenantDataSourceConfig {

    @Bean
    @Primary
    public DataSource dataSource() {

        DataSource juvaryaHospital = DataSourceBuilder.create()
                .url("jdbc:mysql://localhost:3306/juvaryahospital?useUnicode=true&characterEncoding=utf8")
                .username("root")
                .password("root")
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .build();

        DataSource juvaryaHmc = DataSourceBuilder.create()
                .url("jdbc:mysql://localhost:3306/juvaryahmc?useUnicode=true&characterEncoding=utf8")
                .username("root")
                .password("root")
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .build();

        Map<Object, Object> dataSources = new HashMap<>();
        dataSources.put(TenantConstants.DEFAULT_TENANT, juvaryaHospital);
        dataSources.put(TenantConstants.HMC_TENANT, juvaryaHmc);

        MultiTenantRoutingDataSource routingDataSource = new MultiTenantRoutingDataSource();
        routingDataSource.setDefaultTargetDataSource(juvaryaHospital);
        routingDataSource.setTargetDataSources(dataSources);
        routingDataSource.afterPropertiesSet();

        return routingDataSource;
    }
}
