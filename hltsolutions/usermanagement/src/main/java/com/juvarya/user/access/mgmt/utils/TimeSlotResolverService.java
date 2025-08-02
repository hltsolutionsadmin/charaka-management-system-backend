package com.juvarya.user.access.mgmt.utils;

import com.juvarya.user.access.mgmt.dto.enums.TimeSlot;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
public class TimeSlotResolverService {

    public TimeSlot resolveCurrentSlot() {

        ZonedDateTime indiaTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        LocalTime now = indiaTime.toLocalTime();

        if (now.isBefore(LocalTime.of(11, 0))) {
            return TimeSlot.BREAKFAST;
        } else if (now.isBefore(LocalTime.of(17, 0))) {
            return TimeSlot.LUNCH;
        } else {
            return TimeSlot.DINNER;
        }
    }
}
