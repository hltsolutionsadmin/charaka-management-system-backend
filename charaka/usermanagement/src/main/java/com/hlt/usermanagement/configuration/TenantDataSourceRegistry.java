package com.hlt.usermanagement.configuration;

import com.hlt.usermanagement.utils.TenantConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Component
public class TenantDataSourceRegistry {

    @Value("${tenant.default.url}")
    private String defaultUrl;

    @Value("${tenant.default.username}")
    private String defaultUser;

    @Value("${tenant.default.password}")
    private String defaultPass;

    @Value("${tenant.hmc.url}")
    private String hmcUrl;

    @Value("${tenant.hmc.username}")
    private String hmcUser;

    @Value("${tenant.hmc.password}")
    private String hmcPass;

    public Map<String, DataSource> getTenantDataSources() {
        Map<String, DataSource> dataSources = new HashMap<>();

        dataSources.put(TenantConstants.DEFAULT_TENANT,
                createDataSource(defaultUrl, defaultUser, defaultPass));

        dataSources.put(TenantConstants.HMC_TENANT,
                createDataSource(hmcUrl, hmcUser, hmcPass));

        return dataSources;
    }

    private DataSource createDataSource(String url, String username, String password) {
        return DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password)
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .build();
    }
}
