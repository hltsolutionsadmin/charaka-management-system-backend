package com.hlt.auth.exception.handling;

import org.springframework.http.HttpStatus;

public enum ErrorCode {


    USER_NOT_FOUND(1000, "User Not Found", HttpStatus.NOT_FOUND),
    ENQUIRY_NOT_FOUND(2000, "Enquiry Not Found", HttpStatus.NOT_FOUND),
    INVALID_OLD_PASSWORD(1101, "The old password you entered is incorrect", HttpStatus.BAD_REQUEST),
    APPOINTMENT_NOT_FOUND(2001, "Appointment Not Found", HttpStatus.NOT_FOUND),
    PATIENT_NOT_FOUND(2001, "Patient not found", HttpStatus.NOT_FOUND),
    UNAUTHORIZED_BUSINESS_ACCESS(1001, "Unauthorized Bussiness Access", HttpStatus.NOT_FOUND),
    COUPON_CODE_ALREADY_EXISTS(2003, "Coupon code already exists", HttpStatus.NOT_FOUND),
    OFFER_NOT_FOUND(2004, "Offer not found", HttpStatus.NOT_FOUND),
    BUSINESS_ID_REQUIRED(2005, "Bussiness id required", HttpStatus.NOT_FOUND),
    OFFER_NAME_REQUIRED(2006, "Offer name required", HttpStatus.NOT_FOUND),
    OFFER_TYPE_REQUIRED(2007, "Offer type required", HttpStatus.NOT_FOUND),
    OFFER_VALUE_INVALID(2008, "Offer value invalid", HttpStatus.NOT_FOUND),
    INVALID_DATE_RANGE(2009, "Invalid date range", HttpStatus.NOT_FOUND),
    INVALID_FILE(2009, "Invalid file type", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS(1001, "User Already Exists", HttpStatus.CONFLICT),
    EMAIL_ALREADY_IN_USE(1002, "Email Is Already In Use", HttpStatus.CONFLICT),
    UNAUTHORIZED(1003, "Unauthorized Access", HttpStatus.UNAUTHORIZED),
    BUSINESS_NOT_FOUND(1002, "Business not found", HttpStatus.NOT_FOUND),
    BUSINESS_NOT_APPROVED(1002, "Business not Approved", HttpStatus.NOT_FOUND),

    HOSPITAL_ADMIN_ALREADY_EXISTS(2001, "A hospital admin already exists for this hospital", HttpStatus.CONFLICT),

    CATEGORY_NOT_FOUND(3001, "Category not found", HttpStatus.NOT_FOUND),
    CATEGORY_ALREADY_EXISTS(3001, "Category already exists", HttpStatus.NOT_FOUND),
    PRODUCT_NOT_FOUND(3001, "Product not found", HttpStatus.NOT_FOUND),
    PRODUCT_ALREADY_EXISTS(3001, "Product already exists", HttpStatus.NOT_FOUND),
    INVALID_ATTRIBUTE(3001, "Invalid attribute", HttpStatus.NOT_FOUND),
    BUSINESS_TIMING_NOT_FOUND(3001, "Business timimg not found", HttpStatus.NOT_FOUND),
    BUSSINESS_NOT_APPROVED(3001, "Bussiness not approved", HttpStatus.NOT_FOUND),
    INVALID_INPUT(3001, "Invalid input", HttpStatus.NOT_FOUND),


    OTP_EXPIRED(1801, "OTP Expired", HttpStatus.BAD_REQUEST),
    TOKEN_PROCESSING_ERROR(1804, "Error Processing Refresh Token", HttpStatus.INTERNAL_SERVER_ERROR),

    ADDRESS_NOT_FOUND(1901, "Address not found.", HttpStatus.NOT_FOUND),
    INVALID_ADDRESS(1902, "Invalid address data or unauthorized access.", HttpStatus.BAD_REQUEST),
    ACCESS_DENIED(1903, "Access denied —  ownership mismatch for the given user ID.", HttpStatus.BAD_REQUEST),

    NOT_FOUND(2000, "Requested Resource Not Found", HttpStatus.NOT_FOUND),
    BAD_REQUEST(2000, "Bad Request", HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR(2001, "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR),
    FORBIDDEN(2002, "Forbidden", HttpStatus.FORBIDDEN),
    METHOD_NOT_ALLOWED(2003, "Method Not Allowed", HttpStatus.METHOD_NOT_ALLOWED),
    NULL_POINTER(2004, "Null Pointer Exception", HttpStatus.BAD_REQUEST),





    UNAUTHORIZED_REVIEW(3005, "Unauthorized review", HttpStatus.CONFLICT),


    ALREADY_EXISTS(3003, "Resource already exists", HttpStatus.CONFLICT),
    ROLE_NOT_FOUND(3004, "Role not found", HttpStatus.CONFLICT);

    // OTP & Token (1800–1899)


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
