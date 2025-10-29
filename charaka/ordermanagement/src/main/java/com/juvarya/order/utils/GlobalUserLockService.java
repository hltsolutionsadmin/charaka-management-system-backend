package com.juvarya.order.utils;

import com.juvarya.order.dto.enums.LockType;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class GlobalUserLockService {
    private final ConcurrentHashMap<Long, LockInfo> locks = new ConcurrentHashMap<>();

    public boolean tryLock(Long userId, String deviceId, LockType type) {
        return locks.compute(userId, (key, existingLock) -> {
            if (existingLock == null || (existingLock.deviceId.equals(deviceId) && existingLock.type == type)) {
                return new LockInfo(deviceId, type);
            }
            return existingLock;
        }).deviceId.equals(deviceId);
    }

    public void releaseLock(Long userId, String deviceId) {
        locks.computeIfPresent(userId, (key, lockInfo) -> {
            if (lockInfo.deviceId.equals(deviceId)) {
                return null;
            }
            return lockInfo;
        });
    }

    private static class LockInfo {
        String deviceId;
        LockType type;
        LockInfo(String deviceId, LockType type) {
            this.deviceId = deviceId;
            this.type = type;
        }
    }
}
