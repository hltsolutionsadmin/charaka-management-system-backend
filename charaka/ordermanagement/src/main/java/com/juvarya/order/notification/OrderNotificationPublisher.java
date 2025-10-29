package com.juvarya.order.notification;

import com.juvarya.order.dto.notification.OrderNotification;
import com.juvarya.order.dto.notification.OrderNotificationEvent;
import com.juvarya.order.dto.enums.OrderNotificationEventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Publisher for order-related notification events.
 * Uses Spring's ApplicationEventPublisher to publish events asynchronously.
 */
@Component
@Slf4j
public class OrderNotificationPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    public OrderNotificationPublisher(ApplicationEventPublisher eventPublisher) {
        this.applicationEventPublisher = eventPublisher;
    }

    /**
     * Send a single notification
     * 
     * @param notification The notification to send
     * @param eventType The type of notification event
     */
    public void sendNotification(OrderNotification notification, final OrderNotificationEventType eventType) {
        if (notification == null || eventType == null) {
            log.warn("Cannot send notification: notification or eventType is null");
            return;
        }
        log.info("Sending order notification asynchronously: type={}, orderId={}", eventType, notification.getOrderId());
        OrderNotificationEvent event = new OrderNotificationEvent(this, Collections.singletonList(notification), eventType);
        applicationEventPublisher.publishEvent(event);
    }

    /**
     * Send multiple notifications
     * 
     * @param notifications The notifications to send
     * @param eventType The type of notification event
     */
    public void sendNotifications(List<OrderNotification> notifications, final OrderNotificationEventType eventType) {
        if (notifications == null || eventType == null || notifications.isEmpty()) {
            log.warn("Cannot send notifications: notifications is null/empty or eventType is null");
            return;
        }
        log.info("Sending {} order notifications asynchronously: type={}", notifications.size(), eventType);
        OrderNotificationEvent event = new OrderNotificationEvent(this, notifications, eventType);
        applicationEventPublisher.publishEvent(event);
    }
}