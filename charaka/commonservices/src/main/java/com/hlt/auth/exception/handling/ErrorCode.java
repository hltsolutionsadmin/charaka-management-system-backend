package com.hlt.auth.exception.handling;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // ===========================
    // User & Auth Errors (1000–1099)
    // ===========================
    USER_NOT_FOUND(1000, "User Not Found", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS(1001, "User Already Exists", HttpStatus.CONFLICT),
    EMAIL_ALREADY_IN_USE(1002, "Email Is Already In Use", HttpStatus.CONFLICT),
    UNAUTHORIZED(1003, "Unauthorized Access", HttpStatus.UNAUTHORIZED),
    UNSUPPORTED_MAPPING_TYPE(1021, "Unsupported mapping type for the given role", HttpStatus.BAD_REQUEST),

    TELECALLER_MAPPING_LIMIT_EXCEEDED(1022, "A telecaller can only be mapped to a maximum of 2 hospitals.", HttpStatus.BAD_REQUEST),



    // ===========================
    // OTP & Token (1800–1899)
    // ===========================
    OTP_NOT_FOUND(1800, "Unable To Find OTP", HttpStatus.NOT_FOUND),
    OTP_EXPIRED(1801, "OTP Expired", HttpStatus.BAD_REQUEST),
    INVALID_OTP(1802, "Invalid OTP", HttpStatus.BAD_REQUEST),
    INVALID_REFRESH_TOKEN(1803, "Invalid Refresh Token", HttpStatus.BAD_REQUEST),
    INVALID_VOTE_TYPE(1803, "Invalid vote type provided. Allowed values: BRANCH, COLLEGE.", HttpStatus.BAD_REQUEST),
    TOKEN_PROCESSING_ERROR(1804, "Error Processing Refresh Token", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHORIZED_VOTING(1805, "You can only vote for participants enrolled in your own college.", HttpStatus.FORBIDDEN),
    INVALID_ROLE(2003, "Invalid role specified.", HttpStatus.BAD_REQUEST),
    TELECALLER_LIMIT_EXCEEDED(2004, "Telecaller cannot be assigned to more than 2 businesses.", HttpStatus.BAD_REQUEST),
    DUPLICATE_MAPPING(2005, "Mapping already exists for user, business, and role.", HttpStatus.CONFLICT),
    MAPPING_NOT_FOUND(2006, "User role mapping not found.", HttpStatus.NOT_FOUND),

    // ===========================
    // Address & App Info (1900–1999)
    // ===========================
    APPINFO_NOT_FOUND(1900, "App Info Not Found With Given ID", HttpStatus.NOT_FOUND),
    ADDRESS_NOT_FOUND(1901, "Address not found.", HttpStatus.NOT_FOUND),
    INVALID_ADDRESS(1902, "Invalid address data or unauthorized access.", HttpStatus.BAD_REQUEST),
    ACCESS_DENIED(1903, "Access denied —  ownership mismatch for the given user ID.", HttpStatus.BAD_REQUEST),

    // ===========================
    // General Exceptions (2000–2099)
    // ===========================
    NOT_FOUND(2000, "Requested Resource Not Found", HttpStatus.NOT_FOUND),
    BAD_REQUEST(2000, "Bad Request", HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR(2001, "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR),
    TABLE_NOT_FOUND(2001, "Table not found", HttpStatus.NOT_FOUND),
    FORBIDDEN(2002, "Forbidden", HttpStatus.FORBIDDEN),
    METHOD_NOT_ALLOWED(2003, "Method Not Allowed", HttpStatus.METHOD_NOT_ALLOWED),
    NULL_POINTER(2004, "Null Pointer Exception", HttpStatus.BAD_REQUEST),

    // Product, Category, Business (3000–3099)
    // ===========================
    CATEGORY_NOT_FOUND(3001, "Category not found", HttpStatus.NOT_FOUND),
    BUSINESS_NOT_FOUND(3002, "Business not found", HttpStatus.NOT_FOUND),
    PRODUCT_ALREADY_EXISTS(3003, "Product already exists", HttpStatus.CONFLICT),
    ALREADY_EXISTS(3003, "Resource already exists", HttpStatus.CONFLICT),
    RESTAURANT_NOT_APPROVED(3004, "Restaurant not approved", HttpStatus.FORBIDDEN),
    ROLE_NOT_FOUND(3004, "Role not found", HttpStatus.CONFLICT);

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
