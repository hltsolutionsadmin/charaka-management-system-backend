package com.juvarya.commonservice.enums;

public enum ERole {

    // Common Roles
    ROLE_USER,                     // Authenticated base user (restricted access)
    ROLE_USER_ADMIN,              // Platform administrator managing all modules and users
    ROLE_SYSTEM_USER,             // Technical system account (integrations, scheduled jobs)

    // Customer-Facing Roles
    ROLE_CUSTOMER,                // End-user placing orders, booking services, or consuming content

    // Restaurant Domain
    ROLE_RESTAURANT_OWNER,
    ROLE_RESTAURANT_MANAGER,
    ROLE_RESTAURANT_SUB_MANAGER,
    ROLE_RESTAURANT_CAPTAIN,
    ROLE_RESTAURANT_SUB_CAPTAIN,
    ROLE_RESTAURANT_STAFF,
    ROLE_RESTAURANT_WAITER,
    ROLE_RESTAURANT_BILLING_STAFF,
    ROLE_RESTAURANT_KITCHEN_STAFF,
    ROLE_DELIVERY_PARTNER,

    // Training Institute Domain
    ROLE_TRAINING_ADMIN,          // Institute administrator managing courses, trainers, and students
    ROLE_TRAINER,                 // Faculty member delivering training sessions
    ROLE_STUDENT,                 // End-user attending courses and training programs
        // Staff coordinating schedules, batches, and trainer assignments


    // Cross-domain
    ROLE_CUSTOMER_SUPPORT,        // Handles support tickets and live customer interactions
    ROLE_LAND_OWNER,
    ROLE_IT_ADMIN,
    ROLE_IT_MANAGANER,
    ROLE_BUILDER,

    ROLE_COLLEGE_ADMIN,
    ROLE_SOFTWARE_EMPLOYEE ,
    ROLE_SOFTWARE_ADMIN


}

