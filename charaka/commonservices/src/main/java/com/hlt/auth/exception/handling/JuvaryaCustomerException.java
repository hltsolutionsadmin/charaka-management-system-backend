package com.hlt.auth.exception.handling;

import lombok.Getter;

@Getter
public class JuvaryaCustomerException extends RuntimeException {
    private final ErrorCode errorCode;

    public JuvaryaCustomerException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public JuvaryaCustomerException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

}
