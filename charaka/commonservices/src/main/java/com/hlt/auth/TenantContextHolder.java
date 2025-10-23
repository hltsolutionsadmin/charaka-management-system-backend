package com.hlt.auth;

public class TenantContextHolder {

    private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

    public static void setTenant(String tenantId) {
        CONTEXT.set(tenantId);
    }

    public static String getTenant() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
