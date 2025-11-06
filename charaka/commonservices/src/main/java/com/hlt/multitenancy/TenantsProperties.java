package com.hlt.multitenancy;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "tenants")
public class TenantsProperties {

    private String defaultTenant;
    private List<TenantDataSourceProps> datasources = new ArrayList<>();

    public String getDefaultTenant() { return defaultTenant; }
    public void setDefaultTenant(String defaultTenant) { this.defaultTenant = defaultTenant; }

    public List<TenantDataSourceProps> getDatasources() { return datasources; }
    public void setDatasources(List<TenantDataSourceProps> datasources) { this.datasources = datasources; }

    public static class TenantDataSourceProps {
        private String tenantId;
        private String url;
        private String username;
        private String password;
        private String driverClassName;

        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getDriverClassName() { return driverClassName; }
        public void setDriverClassName(String driverClassName) { this.driverClassName = driverClassName; }
    }
}
