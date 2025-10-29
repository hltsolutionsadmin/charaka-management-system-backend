package com.juvarya.order.utils;

import com.juvarya.order.dao.CartRepository;
import com.juvarya.order.entity.CartModel;
import com.juvarya.order.firebase.dto.Notification;
import com.juvarya.order.firebase.dto.NotificationEventType;
import com.juvarya.order.firebase.listeners.NotificationPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CartNotificationScheduler {

    private final CartRepository cartRepository;
    private final NotificationPublisher notificationPublisher;


    @Scheduled(cron = "0 0 9 * * ?")
    public void notifyInactiveCarts() {
        LocalDateTime twoDaysAgo = LocalDateTime.now().minusDays(2);
        List<CartModel> staleCarts = cartRepository.findUnorderedCartsBefore(twoDaysAgo);

        for (CartModel cart : staleCarts) {
            Map<String, String> params = Map.of(
                "businessName", cart.getBusinessName()
            );

            Notification notification = Notification.buildNotification(
                cart.getUserId(),
                cart.getId(),
                NotificationEventType.CART_ABANDONED,
                params
            );

            notificationPublisher.sendNotifications(notification, NotificationEventType.CART_ABANDONED);
        }
    }
}
