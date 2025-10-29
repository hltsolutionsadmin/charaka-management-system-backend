package com.juvarya.order.aspect;

import com.juvarya.order.dto.enums.LockType;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UserLock {
    LockType value();
}
