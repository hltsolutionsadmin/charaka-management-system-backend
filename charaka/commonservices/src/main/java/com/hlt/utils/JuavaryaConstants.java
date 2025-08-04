package com.hlt.utils;

public interface JuavaryaConstants {

    static final String _0 = "0";
    static final String _20 = "20";
    static final String TOTAL_PAGES = "totalPages";
    static final String TOTAL_ITEMS = "totalItems";
    static final String CURRENT_PAGE = "currentPage";
    static final String PROFILES = "data";
    static final String OR = "or";
    static final String BEARER = "Bearer ";
    static final String PAGE_NUM = "pageNo";
    static final String PAGE_SIZE = "pageSize";
    static final String MULTIBLOCK = "MULTIBLOCK";
    static final String SINGLE = "SINGLE";

    // Role expressions
    static final String ROLE_USER = "hasRole('ROLE_USER')";
    static final String ROLE_USER_ADMIN = "hasRole('ROLE_USER_ADMIN')";
    static final String ROLE_USER_ADMINS = "ROLE_USER_ADMIN";
    static final String ROLE_STUDENT = "hasRole('ROLE_STUDENT')";
    static final String ROLE_ROLE_BUILDER = "hasRole('ROLE_BUILDER')";
    static final String ROLE_LAND_OWNER = "hasRole('ROLE_LAND_OWNER')";

    // Direct role names
    public static final String ROLE_BUILDER = "ROLE_BUILDER";

    // New roles
    static final String ROLE_SUPER_ADMIN = "hasRole('ROLE_SUPER_ADMIN')";
    static final String ROLE_HOSPITAL_ADMIN = "hasRole('ROLE_HOSPITAL_ADMIN')";
    static final String ROLE_DOCTOR = "hasRole('ROLE_DOCTOR')";
    static final String ROLE_RECEPTIONIST = "hasRole('ROLE_RECEPTIONIST')";
    static final String ROLE_HOSPITAL_MANAGER = "hasRole('ROLE_HOSPITAL_MANAGER')";
    static final String ROLE_TELECALLER = "hasRole('ROLE_TELECALLER')";
    static final String ROLE_CALL_SUPERVISOR = "hasRole('ROLE_CALL_SUPERVISOR')";

    // Statuses
    static final String VERIFIED = "Verified";
    static final String FAILED = "failed";
    static final String SUCCESS = "Success";
    static final String PENDING = "PENDING";
    static final String REFUNDED = "REFUNDED";
    static final String PROCESSING = "PROCESSING";

    static final String DASHBOARD = "mediaType";
}
