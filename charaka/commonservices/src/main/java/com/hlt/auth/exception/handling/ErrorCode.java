package com.hlt.auth.exception.handling;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // ===========================
    // User & Auth Errors (1000–1099)
    // ===========================
    USER_NOT_FOUND(1000, "User Not Found", HttpStatus.NOT_FOUND),
    UNAUTHORIZED_BUSINESS_ACCESS(1001, "Unauthorized Bussiness Access", HttpStatus.NOT_FOUND),

    ENQUIRY_NOT_FOUND(2000, "Enquiry Not Found", HttpStatus.NOT_FOUND),
    INVALID_OLD_PASSWORD(1101, "The old password you entered is incorrect", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(1100, "Invalid Token", HttpStatus.BAD_REQUEST),
    TOKEN_EXPIRED(1101, "Token Expired", HttpStatus.UNAUTHORIZED),
    APPOINTMENT_NOT_FOUND(2001, "Appointment Not Found", HttpStatus.NOT_FOUND),
    PATIENT_NOT_FOUND(2002, "Patient not found", HttpStatus.NOT_FOUND),
    COUPON_CODE_ALREADY_EXISTS(2003, "Coupon code already exists", HttpStatus.NOT_FOUND),
    OFFER_NOT_FOUND(2004, "Offer not found", HttpStatus.NOT_FOUND),
    BUSINESS_ID_REQUIRED(2005, "Bussiness id required", HttpStatus.NOT_FOUND),
    OFFER_NAME_REQUIRED(2006, "Offer name required", HttpStatus.NOT_FOUND),
    OFFER_TYPE_REQUIRED(2007, "Offer type required", HttpStatus.NOT_FOUND),
    OFFER_VALUE_INVALID(2008, "Offer value invalid", HttpStatus.NOT_FOUND),
    INVALID_DATE_RANGE(2009, "Invalid date range", HttpStatus.NOT_FOUND),

    // Appointment related errors
    USER_ALREADY_EXISTS(1001, "User Already Exists", HttpStatus.CONFLICT),
    EMAIL_ALREADY_IN_USE(1002, "Email Is Already In Use", HttpStatus.CONFLICT),
    UNAUTHORIZED(1003, "Unauthorized Access", HttpStatus.UNAUTHORIZED),
    UNSUPPORTED_MAPPING_TYPE(1021, "Unsupported mapping type for the given role", HttpStatus.BAD_REQUEST),
    MAPPING_ALREADY_DEACTIVATED(1002, "User mapping is already deactivated", HttpStatus.UNPROCESSABLE_ENTITY),
    BUSINESS_NOT_FOUND(1002, "Business not found", HttpStatus.NOT_FOUND),
    BUSINESS_NOT_APPROVED(1002, "Business not Approved", HttpStatus.NOT_FOUND),

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
    CATEGORY_ALREADY_EXISTS(3001, "Category already exists", HttpStatus.NOT_FOUND),
    PRODUCT_NOT_FOUND(3001, "Product not found", HttpStatus.NOT_FOUND),
    PRODUCT_ALREADY_EXISTS(3001, "Product already exists", HttpStatus.NOT_FOUND),
    INVALID_ATTRIBUTE(3001, "Invalid attribute", HttpStatus.NOT_FOUND),
    BUSINESS_TIMING_NOT_FOUND(3001, "Business timimg not found", HttpStatus.NOT_FOUND),
    BUSSINESS_NOT_APPROVED(3001, "Bussiness not approved", HttpStatus.NOT_FOUND),
    INVALID_INPUT(3001, "Invalid input", HttpStatus.NOT_FOUND),

    ORDER_NOT_FOUND(3001, "Order not found", HttpStatus.NOT_FOUND),
    INVALID_ORDER_REQUEST(3002, "Invalid order request", HttpStatus.NOT_FOUND),
    DELIVERY_ASSIGNMENT_FAILED(3003, "Delivery assignment failed", HttpStatus.NOT_FOUND),
    CART_NOT_FOUND(3004, "Cart not found", HttpStatus.NOT_FOUND),
    PAYMENT_TRANSACTION_ID_REQUIRED(3005, "Payment transaction id request", HttpStatus.NOT_FOUND),

    PAYMENT_NOT_FOUND(3005, "Payment not found", HttpStatus.NOT_FOUND),
    PAYMENT_AMOUNT_MISMATCH(3006, "Payment amount mismatch", HttpStatus.NOT_FOUND),
    REFUND_FAILED(3007, "Refund failed", HttpStatus.NOT_FOUND),
    ALREADY_REFUNDED(3008, "Already refunded", HttpStatus.NOT_FOUND),
    DUPLICATE_PAYMENT(3009, "Duplicate payment", HttpStatus.NOT_FOUND),

    SHOPIFY_SYNC_FAILED(4001, "Shopify sync failed", HttpStatus.NOT_FOUND),
    INVALID_REQUEST(4002, "Invalid request", HttpStatus.NOT_FOUND),
    DEVICE_ID_HEADER_MISSING(4003, "Device id header missing", HttpStatus.NOT_FOUND),
    LOCKED_ON_ANOTHER_DEVICE(4004, "Locked on another device", HttpStatus.NOT_FOUND),

    INVALID_PRICE(3003, "Invalid price", HttpStatus.CONFLICT),
    INVALID_PASSWORD(3004, "Invalid password", HttpStatus.CONFLICT),
    UNAUTHORIZED_REVIEW(3005, "Unauthorized review", HttpStatus.CONFLICT),
    INVALID_CART_ITEM(3006, "Invalid cart item", HttpStatus.CONFLICT),
    CART_ITEM_NOT_FOUND(3007, "Cart item not found", HttpStatus.CONFLICT),
    INVALID_PRODUCT_DATA(3008, "Invalid product data", HttpStatus.CONFLICT),
    INVALID_PRODUCT_ID(3009, "Invalid product id", HttpStatus.CONFLICT),
    INVALID_OFFER_ID(3009, "Invalid offer id", HttpStatus.CONFLICT),
    OFFER_NOT_APPLICABLE(3009, "Offer not applicable", HttpStatus.CONFLICT),
    INVALID_BUSINESS(3009, "Invalid business", HttpStatus.CONFLICT),
    COMPLAINT_NOT_FOUND(3009, "Complaint not found", HttpStatus.CONFLICT),


    ALREADY_EXISTS(3003, "Resource already exists", HttpStatus.CONFLICT),
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
