package com.hlt.usermanagement.configuration;

import com.hlt.usermanagement.utils.TenantConstants;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Component
public class TenantDataSourceRegistry {

    public Map<String, DataSource> getTenantDataSources() {
        Map<String, DataSource> dataSources = new HashMap<>();

        dataSources.put(TenantConstants.DEFAULT_TENANT,
                createDataSource("jdbc:mysql://localhost:3306/juvaryahospital?useUnicode=true&characterEncoding=utf8"));

        dataSources.put(TenantConstants.HMC_TENANT,
                createDataSource("jdbc:mysql://localhost:3306/juvaryahmc?useUnicode=true&characterEncoding=utf8"));

        return dataSources;
    }

    private DataSource createDataSource(String url) {
        return DataSourceBuilder.create()
                .url(url)
                .username("root")
                .password("root")
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .build();
    }
}
