package com.juvarya.order.dto.notification;

import com.juvarya.order.dto.enums.OrderNotificationEventType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * Event for order-related notifications.
 * Used to publish notification events that will be handled by a listener.
 */
@Getter
@Setter
public class OrderNotificationEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;
    
    private final List<OrderNotification> notifications;
    private final OrderNotificationEventType notificationEventType;

    /**
     * Create a new OrderNotificationEvent
     * 
     * @param source The source of the event
     * @param notifications The notifications to send
     * @param notificationEventType The type of notification event
     */
    public OrderNotificationEvent(Object source, List<OrderNotification> notifications, OrderNotificationEventType notificationEventType) {
        super(source);
        this.notifications = notifications;
        this.notificationEventType = notificationEventType;
    }
}