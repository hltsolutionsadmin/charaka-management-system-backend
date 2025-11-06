package com.hlt.productmanagement.service;

import com.hlt.auth.exception.handling.ErrorCode;
import com.hlt.auth.exception.handling.HltCustomerException;
import com.hlt.productmanagement.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class ProductAvailabilityScheduler {

    @Autowired
    private OfferService offerService;

    @Autowired
    private ProductRepository productRepository;

    private final ZoneId ZONE_IST = ZoneId.of("Asia/Kolkata");
    private final Map<Long, ZonedDateTime> productEndTimeMap = new ConcurrentHashMap<>();

    public void scheduleReactivation(Long productId, String endTimeStr) {
        try {
            LocalTime endTime = LocalTime.parse(endTimeStr);
            ZonedDateTime zonedEndTime = ZonedDateTime.of(LocalDate.now(ZONE_IST), endTime, ZONE_IST);
            productEndTimeMap.put(productId, zonedEndTime);
            log.info("Reactivation scheduled: productId={}, endTime={}", productId, zonedEndTime.toLocalTime());
        } catch (DateTimeParseException e) {
            throw new HltCustomerException(ErrorCode.INVALID_ATTRIBUTE,
                    "Invalid time format for endTime. Expected format: HH:mm");
        }
    }

    @Scheduled(fixedDelay = 60000)
    public void checkAndActivateProducts() {
        ZonedDateTime now = ZonedDateTime.now(ZONE_IST);

        productEndTimeMap.forEach((productId, endTime) -> {
            if (now.isAfter(endTime)) {
                productRepository.findById(productId).ifPresent(product -> {
                    if (!Boolean.TRUE.equals(product.isAvailable())) {
                        product.setAvailable(true);
                        productRepository.save(product);
                        log.info("Product reactivated: productId={}", productId);
                    }
                });
                productEndTimeMap.remove(productId);
            }
        });
    }

    @PostConstruct
    public void initScheduler() {
        log.info("ProductAvailabilityScheduler initialized. Scheduled products: {}", productEndTimeMap.size());
    }

    @Scheduled(cron = "0 0 * * * *")
    public void scheduledExpireOffers() {
        offerService.expirePastOffers();
    }
}
