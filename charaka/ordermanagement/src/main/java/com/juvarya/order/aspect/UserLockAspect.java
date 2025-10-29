package com.juvarya.order.aspect;

import com.hlt.auth.exception.handling.ErrorCode;
import com.hlt.auth.exception.handling.HltCustomerException;
import com.hlt.commonservice.user.UserDetailsImpl;
import com.juvarya.order.dto.enums.LockType;
import com.juvarya.order.utils.GlobalUserLockService;
import com.hlt.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class UserLockAspect {

    private final GlobalUserLockService lockService;
    private final HttpServletRequest request;

    @Around("@annotation(userLock)")
    public Object aroundLockedMethod(ProceedingJoinPoint joinPoint, UserLock userLock) throws Throwable {
        UserDetailsImpl user = SecurityUtils.getCurrentUserDetails();
        String deviceId = request.getHeader("X-Device-Id");

        if (deviceId == null || deviceId.isBlank()) {
            throw new HltCustomerException(ErrorCode.DEVICE_ID_HEADER_MISSING);
        }

        Long userId = user.getId();
        LockType type = userLock.value();

        if (!lockService.tryLock(userId, deviceId, type)) {
            throw new HltCustomerException(ErrorCode.LOCKED_ON_ANOTHER_DEVICE);
        }

        try {
            return joinPoint.proceed();
        } finally {
            lockService.releaseLock(userId, deviceId);
        }
    }
}
