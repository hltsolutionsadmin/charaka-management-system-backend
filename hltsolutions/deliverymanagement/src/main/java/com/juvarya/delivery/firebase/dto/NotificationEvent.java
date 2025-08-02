package com.juvarya.delivery.firebase.dto;


import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
@Setter
public class NotificationEvent extends ApplicationEvent{
	private static final long serialVersionUID = 1L;
    private final List<Notification> notifications;
    private final NotificationEventType notificationEventType;

    public NotificationEvent(Object source, List<Notification> notifications, NotificationEventType notificationEventType) {
        super(source);
        this.notifications = notifications;
        this.notificationEventType = notificationEventType;
    }
}
