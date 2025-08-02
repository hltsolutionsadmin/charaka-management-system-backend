package com.juvarya.auth.exception.handling;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // ===========================
    // User & Auth Errors (1000–1099)
    // ===========================
    USER_NOT_FOUND(1000, "User Not Found", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS(1001, "User Already Exists", HttpStatus.CONFLICT),
    EMAIL_ALREADY_IN_USE(1002, "Email Is Already In Use", HttpStatus.CONFLICT),
    UNAUTHORIZED(1003, "Unauthorized Access", HttpStatus.UNAUTHORIZED),
    USER_NOT_RESTAURANT_ADMIN(1004, "User is not a valid Restaurant Admin", HttpStatus.FORBIDDEN),
    UNAUTHORIZED_REVIEW(1005, "User is not allowed to review this product", HttpStatus.UNAUTHORIZED),
    NO_DELIVERY_PARTNER_AVAILABLE(1130, "No delivery partner is available", HttpStatus.NOT_FOUND),
    DELIVERY_PARTNER_ALREADY_REGISTERED(2001, "Delivery Partner already registered", HttpStatus.BAD_REQUEST),
    ORDER_ACCEPTANCE_FAILED(1304, "Failed to accept the order", HttpStatus.BAD_REQUEST),
    DELIVERY_RECORD_NOT_FOUND(2003, "Delivery record not found", HttpStatus.NOT_FOUND),
    ORDER_STATUS_UPDATE_FAILED(2100, "Failed to update order status", HttpStatus.INTERNAL_SERVER_ERROR),
    REMOTE_ORDER_UPDATE_FAILED(2102, "Failed to update remote order status", HttpStatus.BAD_GATEWAY),
    INVALID_IP_ADDRESS(2102, "Failed to update remote order status", HttpStatus.BAD_GATEWAY),



    // ===========================
    // Common Validation (1100–1199)
    // ===========================
    CITY_NOT_FOUND(1100, "City Not Found", HttpStatus.NOT_FOUND),
    INVALID_CART_ITEM(1111, "Invalid cart item", HttpStatus.BAD_REQUEST),
    INVALID_INPUT(1101, "Invalid Input Provided", HttpStatus.BAD_REQUEST),
    WALLET_NOT_FOUND(1102, "Wallet not found.", HttpStatus.NOT_FOUND),
    PAYMENT_NOT_FOUND(1103, "Payment not found", HttpStatus.NOT_FOUND),
    ALREADY_REFUNDED(1104, "Payment already refunded", HttpStatus.CONFLICT),
    PAYMENT_TRANSACTION_ID_REQUIRED(1106, "Payment transaction ID is required", HttpStatus.BAD_REQUEST),
    INVALID_ORDER_REQUEST(1107, "Invalid order request. REORDER number is mandatory.", HttpStatus.BAD_REQUEST),
    DUPLICATE_PAYMENT(1108, "Duplicate payment", HttpStatus.CONFLICT),
    REFUND_FAILED(1105, "Refund failed", HttpStatus.BAD_REQUEST),
    COMPLAINT_NOT_FOUND(1109, "Complaint not found", HttpStatus.NOT_FOUND),
    PAYMENT_AMOUNT_MISMATCH(1110, "Payment amount mismatch with Razorpay", HttpStatus.BAD_REQUEST),
    INVALID_ATTRIBUTE(1111, "Invalid attribute", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1112, "Invalid password provided", HttpStatus.UNAUTHORIZED),
    INVALID_OFFER_ID(1103, "Invalid or inaccessible offer ID", HttpStatus.BAD_REQUEST),
    OFFER_NOT_APPLICABLE(1104, "Offer is not applicable to this cart", HttpStatus.BAD_REQUEST),
    KOT_SEQUENCE_NOT_FOUND(1105, "KOT sequence not found for the current financial year", HttpStatus.NOT_FOUND),
    TARGET_TYPE_REQUIRED(1111, "Offer target type is required", HttpStatus.BAD_REQUEST),
    PRODUCT_IDS_REQUIRED(1112, "Product IDs are required for product-based offer", HttpStatus.BAD_REQUEST),
    CATEGORY_IDS_REQUIRED(1113, "Category IDs are required for category-based offer", HttpStatus.BAD_REQUEST),
    PUJA_NOT_FOUND(2000, "Puja not found", HttpStatus.NOT_FOUND),
    BUSINESS_TIMING_NOT_FOUND(1120, "Required business timing is missing", HttpStatus.BAD_REQUEST),
    LOCKED_ON_ANOTHER_DEVICE(1121, "Operation not allowed from this device. Already locked.", HttpStatus.FORBIDDEN),
    DEVICE_ID_HEADER_MISSING(1122, "X-Device-Id header is missing", HttpStatus.BAD_REQUEST),

    // ===========================
    // API Key & Sandbox (1200–1299)
    // ===========================
    INVALID_API_KEY(1200, "Invalid API key provided", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED_API_KEY(1201, "API Key is invalid or unauthorized", HttpStatus.UNAUTHORIZED),
    CATEGORY_ALREADY_EXISTS(1201, "Category Already Exists", HttpStatus.CONFLICT),
    LEAVE_NOT_FOUND(1201, "Leave not found", HttpStatus.NOT_FOUND),
    SANDBOX_USER_NOT_ALLOWED(1202, "Sandbox user is not allowed to access this resource", HttpStatus.FORBIDDEN),
    EMPLOYEE_NOT_FOUND(1202, "Employee not found", HttpStatus.NOT_FOUND),
    API_KEY_EXPIRED(1203, "API key has expired", HttpStatus.UNAUTHORIZED),
    API_KEY_LIMIT_REACHED(1204, "API key generation limit reached", HttpStatus.TOO_MANY_REQUESTS),
    API_KEY_GENERATION_FAILED(1204, "Failed to generate API key", HttpStatus.INTERNAL_SERVER_ERROR),
    LEAVES_NOT_FOUND_FOR_DATE(1204, "No leaves applied on the given date", HttpStatus.NOT_FOUND),
    SANDBOX_DATA_ACCESS_DENIED(1205, "You are not allowed to access another user's data", HttpStatus.FORBIDDEN),
    UNAUTHORIZED_BUSINESS_ACCESS(1300, "You are not authorized to access this business", HttpStatus.FORBIDDEN),

    IP_ADDRESS_MISMATCH(1301, "IP address mismatch", HttpStatus.FORBIDDEN),

    // BUSINESS_NOT_APPROVED(1301, "Business is not yet approved", HttpStatus.FORBIDDEN),
    BUSINESS_ID_MISSING(1302, "Business ID is required", HttpStatus.BAD_REQUEST),
    KOT_ENTRY_NOT_FOUND(1303, "KOT Entry not found", HttpStatus.NOT_FOUND),
    COUPON_CODE_ALREADY_EXISTS(1106, "Coupon code already exists", HttpStatus.CONFLICT),


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
    NULL_POINTER_EXCEPTION(2004, "Null Pointer Exception", HttpStatus.BAD_REQUEST),
    JSON_CONVERSION_ERROR(2005, "JSON Conversion Exception", HttpStatus.BAD_REQUEST),
    RESTAURANT_NOT_FOUND(2005, "Restaurant not found", HttpStatus.NOT_FOUND),
    NOT_SUPPORTED(2006, "Operation Not Supported", HttpStatus.NOT_ACCEPTABLE),
    DUPLICATE_ENTRY(2007, "Duplicate Entry", HttpStatus.CONFLICT),
    NOT_ALLOWED(2008, "Operation Not Allowed", HttpStatus.FORBIDDEN),
    INSUFFICIENT_DETAILS(2009, "Insufficient Details Provided", HttpStatus.BAD_REQUEST),
    INVALID_READING_VALUE(2010, "Invalid Reading Value", HttpStatus.BAD_REQUEST),
    REQUIRED_CONTACT(2011, "Primary Contact Is Required", HttpStatus.BAD_REQUEST),
    REQUIRED_TYPE(2012, "Required Type Is Missing", HttpStatus.BAD_REQUEST),
    ALREADY_APPROVED(2013, "Already Approved", HttpStatus.CONFLICT),
    ALREADY_OWNER(2014, "User Is Already Flat Owner", HttpStatus.CONFLICT),
    RELATED_USER_NOT_FOUND(2015, "Related User Not Found", HttpStatus.NOT_FOUND),
    PARTNER_NOT_FOUND(2016, "Partner Not Found", HttpStatus.NOT_FOUND),
    NOT_ENOUGH_COINS(2017, "Not Enough Coins For Transaction", HttpStatus.FORBIDDEN),
    DELIVERY_NOT_FOUND(3001, "Delivery not found", HttpStatus.NOT_FOUND),
    NO_ACTIVE_PARTNER(3002, "No active delivery partner available", HttpStatus.BAD_REQUEST),
    INVALID_DELIVERY_STATUS(3003, "Invalid delivery status provided", HttpStatus.BAD_REQUEST),
    DELIVERY_ALREADY_EXISTS(3005, "Delivery already exists for this order", HttpStatus.CONFLICT),
    DELIVERY_ASSIGNMENT_FAILED(3006, "Failed to assign delivery partner", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHORIZED_DELIVERY_UPDATE(3007, "You are not authorized to update delivery status", HttpStatus.FORBIDDEN),
    DELIVERY_PARTNER_NOT_FOUND(2000, "Delivery Partner Not Found", HttpStatus.NOT_FOUND),
    // ===========================
    // Product, Category, Business (3000–3099)
    // ===========================
    CATEGORY_NOT_FOUND(3001, "Category not found", HttpStatus.NOT_FOUND),
    BUSINESS_NOT_FOUND(3002, "Business not found", HttpStatus.NOT_FOUND),
    PRODUCT_ALREADY_EXISTS(3003, "Product already exists", HttpStatus.CONFLICT),
    ALREADY_EXISTS(3003, "Resource already exists", HttpStatus.CONFLICT),
    RESTAURANT_NOT_APPROVED(3004, "Restaurant not approved", HttpStatus.FORBIDDEN),
    ROLE_NOT_FOUND(3004, "Role not found", HttpStatus.CONFLICT),
    PRODUCT_NOT_FOUND(3005, "Product not found", HttpStatus.OK),
    ORDER_NOT_FOUND(3007, "Order not found", HttpStatus.NOT_FOUND),
    INVALID_PAYMENT_TYPE(3008, "Invalid Payment Id", HttpStatus.NOT_FOUND),
    INVALID_ORDER_STATUS(3009, "Invalid order status", HttpStatus.BAD_REQUEST),
    SHOPIFY_SYNC_FAILED(3010, "Shopify sync failed", HttpStatus.BAD_REQUEST),
    PACKAGE_ALREADY_EXISTS(3011, "Package already exists", HttpStatus.BAD_REQUEST),
    INVALID_CATEGORY_TYPE(3004, "Invalid category type", HttpStatus.BAD_REQUEST),

    // ===========================
    // Cart & Order Flow (4000–4099)
    // ===========================
    CART_NOT_FOUND(4001, "Cart not found", HttpStatus.NOT_FOUND),
    CART_ITEM_NOT_FOUND(4002, "Cart item not found", HttpStatus.NOT_FOUND),
    INVALID_REQUEST(4003, "Invalid request", HttpStatus.BAD_REQUEST),
    INVALID_PRODUCT_ID(4004, "Invalid product ID", HttpStatus.BAD_REQUEST),
    INVALID_PRODUCT_DATA(4005, "Product data is incomplete or invalid", HttpStatus.BAD_REQUEST),
    INVALID_BUSINESS_CATEGORY(4007, "Only businesses in the 'Software' category are allowed", HttpStatus.BAD_REQUEST),
    BUSINESS_NOT_APPROVED(4008, "Business is not approved", HttpStatus.FORBIDDEN),
    INVALID_BUSINESS(4009, "All cart items must be from the same business", HttpStatus.FORBIDDEN),
    CONTEST_NOT_FOUND(4010, "Contest not found", HttpStatus.NOT_FOUND),

    // ===========================
    // Contest & Voting (4100–4199)
    // ===========================
    ENROLLMENT_NOT_FOUND(4101, "Enrollment not found", HttpStatus.NOT_FOUND),
    ALREADY_ENROLLED(4102, "Student already enrolled in this contest", HttpStatus.CONFLICT),
    ALREADY_VOTED(4103, "User already voted", HttpStatus.CONFLICT),

    // ===========================
    // Internship (5000–5099)
    // ===========================
    INVALID_INTERNSHIP_REQUEST(5001, "Invalid internship request", HttpStatus.BAD_REQUEST),
    INTERNSHIP_NOT_FOUND(5002, "Internship not found", HttpStatus.NOT_FOUND),
    INTERNSHIP_ALREADY_EXISTS(5003, "Internship with the same code already exists", HttpStatus.CONFLICT),
    INVALID_INTERNSHIP_DATA(5004, "Internship data is incomplete or invalid", HttpStatus.BAD_REQUEST),
    MEDIA_UPLOAD_FAILED(5005, "Failed to upload internship media", HttpStatus.INTERNAL_SERVER_ERROR),
    COMPANY_NOT_APPROVED(5006, "Company not approved for posting internships", HttpStatus.FORBIDDEN),
    INVALID_PRICE(5008, "Price should be entered", HttpStatus.BAD_REQUEST),


    OFFER_NOT_FOUND(4001, "Offer not found", HttpStatus.NOT_FOUND),
    OFFER_NAME_REQUIRED(4002, "Offer name is required", HttpStatus.BAD_REQUEST),
    OFFER_TYPE_REQUIRED(4003, "Offer type is required", HttpStatus.BAD_REQUEST),
    OFFER_VALUE_INVALID(4004, "Offer value must be positive", HttpStatus.BAD_REQUEST),
    INVALID_DATE_RANGE(4005, "End date must not be before start date", HttpStatus.BAD_REQUEST),
    BUSINESS_ID_REQUIRED(4006, "Business ID is required", HttpStatus.BAD_REQUEST);


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
