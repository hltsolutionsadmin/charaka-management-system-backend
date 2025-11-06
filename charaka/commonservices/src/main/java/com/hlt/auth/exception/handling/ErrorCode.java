package com.hlt.auth.exception.handling;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // ===========================
    // User & Auth Errors (1000–1099)
    // ===========================
    USER_NOT_FOUND(1000, "User Not Found", HttpStatus.NOT_FOUND),
    ENQUIRY_NOT_FOUND(2000, "Enquiry Not Found", HttpStatus.NOT_FOUND),
    INVALID_OLD_PASSWORD(1101, "The old password you entered is incorrect", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(1100, "Invalid Token", HttpStatus.BAD_REQUEST),
    TOKEN_EXPIRED(1101, "Token Expired", HttpStatus.UNAUTHORIZED),
    APPOINTMENT_NOT_FOUND(2001, "Appointment Not Found", HttpStatus.NOT_FOUND),
    PATIENT_NOT_FOUND(2001, "Patient not found", HttpStatus.NOT_FOUND),
    CATEGORY_ALREADY_EXISTS(2001, "Category already exists", HttpStatus.CONFLICT),

    // Appointment related errors
    USER_ALREADY_EXISTS(1001, "User Already Exists", HttpStatus.CONFLICT),
    EMAIL_ALREADY_IN_USE(1002, "Email Is Already In Use", HttpStatus.CONFLICT),
    UNAUTHORIZED(1003, "Unauthorized Access", HttpStatus.UNAUTHORIZED),
    UNSUPPORTED_MAPPING_TYPE(1021, "Unsupported mapping type for the given role", HttpStatus.BAD_REQUEST),
    MAPPING_ALREADY_DEACTIVATED(1002, "User mapping is already deactivated", HttpStatus.UNPROCESSABLE_ENTITY),
    BUSINESS_NOT_FOUND(1002, "Business not found", HttpStatus.NOT_FOUND),
    MAPPING_NOT_FOUND(1003, "Mapping not found", HttpStatus.NOT_FOUND),
    TELECALLER_MAPPING_LIMIT_EXCEEDED(1005, "Telecaller cannot be mapped to more than 2 hospitals", HttpStatus.CONFLICT),
    INVALID_ROLE(1007, "Invalid role provided", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED_OPERATION(1016, "Unauthorized operation", HttpStatus.UNAUTHORIZED),
    INVALID_ROLE_FOR_OPERATION(1017, "Invalid role for this operation", HttpStatus.FORBIDDEN),
    HOSPITAL_ADMIN_ALREADY_EXISTS(2001, "A hospital admin already exists for this hospital", HttpStatus.CONFLICT),

    UNAUTHORIZED_ROLE_ASSIGNMENT(1006, "You are not allowed to assign this role", HttpStatus.FORBIDDEN),

    // ===========================
    // OTP & Token (1800–1899)
    // ===========================
    OTP_EXPIRED(1801, "OTP Expired", HttpStatus.BAD_REQUEST),
    TOKEN_PROCESSING_ERROR(1804, "Error Processing Refresh Token", HttpStatus.INTERNAL_SERVER_ERROR),

    // ===========================
    // Address & App Info (1900–1999)
    // ===========================
    ADDRESS_NOT_FOUND(1901, "Address not found.", HttpStatus.NOT_FOUND),
    INVALID_ADDRESS(1902, "Invalid address data or unauthorized access.", HttpStatus.BAD_REQUEST),
    ACCESS_DENIED(1903, "Access denied —  ownership mismatch for the given user ID.", HttpStatus.BAD_REQUEST),

    // ===========================
    // General Exceptions (2000–2099)
    // ===========================
    NOT_FOUND(2000, "Requested Resource Not Found", HttpStatus.NOT_FOUND),
    BAD_REQUEST(2000, "Bad Request", HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR(2001, "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR),
    FORBIDDEN(2002, "Forbidden", HttpStatus.FORBIDDEN),
    METHOD_NOT_ALLOWED(2003, "Method Not Allowed", HttpStatus.METHOD_NOT_ALLOWED),
    NULL_POINTER(2004, "Null Pointer Exception", HttpStatus.BAD_REQUEST),

    // Product, Category, Business (3000–3099)
    // ===========================
    CATEGORY_NOT_FOUND(3001, "Category not found", HttpStatus.NOT_FOUND),
    ALREADY_EXISTS(3003, "Resource already exists", HttpStatus.CONFLICT),
    ROLE_NOT_FOUND(3004, "Role not found", HttpStatus.CONFLICT),

    // Product specific
    PRODUCT_NOT_FOUND(3005, "Product not found", HttpStatus.NOT_FOUND),
    PRODUCT_ALREADY_EXISTS(3006, "Product already exists", HttpStatus.CONFLICT),
    INVALID_ATTRIBUTE(3014, "Invalid attribute", HttpStatus.BAD_REQUEST),
    INVALID_INPUT(3015, "Invalid input", HttpStatus.BAD_REQUEST),
    INVALID_FILE(3021, "Invalid file", HttpStatus.BAD_REQUEST),
    BUSINESS_TIMING_NOT_FOUND(3016, "Business timing not found", HttpStatus.NOT_FOUND),

    // Business access/approval
    UNAUTHORIZED_BUSINESS_ACCESS(3018, "Unauthorized business access", HttpStatus.UNAUTHORIZED),
    BUSINESS_NOT_APPROVED(3019, "Business not approved", HttpStatus.FORBIDDEN),
    RESTAURANT_NOT_APPROVED(3020, "Restaurant not approved", HttpStatus.FORBIDDEN),
    BUSINESS_ID_REQUIRED(3009, "Business ID is required", HttpStatus.BAD_REQUEST),

    // Offer specific
    OFFER_NOT_FOUND(3007, "Offer not found", HttpStatus.NOT_FOUND),
    COUPON_CODE_ALREADY_EXISTS(3008, "Coupon code already exists", HttpStatus.CONFLICT),
    OFFER_NAME_REQUIRED(3010, "Offer name is required", HttpStatus.BAD_REQUEST),
    OFFER_TYPE_REQUIRED(3011, "Offer type is required", HttpStatus.BAD_REQUEST),
    OFFER_VALUE_INVALID(3012, "Offer value is invalid", HttpStatus.BAD_REQUEST),
    INVALID_DATE_RANGE(3013, "Invalid date range", HttpStatus.BAD_REQUEST),

    // Review specific
    UNAUTHORIZED_REVIEW(3017, "User is not authorized to submit review", HttpStatus.FORBIDDEN);

    // ===========================
    // Fields
    // ===========================
    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String formatMessage(Object... args) {
        return String.format(message, args);
    }
}
