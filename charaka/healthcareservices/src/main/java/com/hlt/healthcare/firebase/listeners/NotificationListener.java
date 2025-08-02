package com.hlt.delivery.firebase.listeners;

import com.hlt.auth.JwtUtils;
import com.hlt.commonservice.dto.UserDTO;

import com.hlt.delivery.client.FeignRequestContext;
import com.hlt.delivery.client.UserMgmtClient;
import com.hlt.delivery.firebase.FCMService;
import com.hlt.delivery.firebase.dto.Notification;
import com.hlt.delivery.firebase.dto.NotificationEvent;
import com.hlt.delivery.firebase.dto.NotificationEventType;
import com.hlt.delivery.firebase.dto.NotificationRequest;
import com.hlt.delivery.model.NotificationModel;
import com.hlt.delivery.service.NotificationService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Date;

@EnableAsync
@Component
@Transactional
@Slf4j
public class NotificationListener implements ApplicationListener<NotificationEvent> {


	@Autowired
	private JwtUtils jwtUtils;

	@Autowired
	private FCMService fcmService;
	@Autowired
	private UserMgmtClient userMgmtClient;

	@Autowired
	private NotificationService notificationService;



	@Override
	public void onApplicationEvent(NotificationEvent event) {

		try {
			handleEvent(event);
		} catch (Exception e) {
			log.error("Failed to send notification ", e);
		} finally {
			FeignRequestContext.clearAuthorizationHeader();
		}

	}

	private void handleEvent(NotificationEvent event) { // 21.40
		if (null == event || CollectionUtils.isEmpty(event.getNotifications())
				|| null == event.getNotificationEventType()) {
			log.warn("Invalid kafka");
			return;
		}

		NotificationRequest notificationRequest = new NotificationRequest();

//		
//		  String systemUserToken = jwtUtils.getSystemUserToken();
//		  FeignRequestContext.setAuthorizationHeader("Bearer " + systemUserToken);
		  
		 
		saveAndSendNotification(event, notificationRequest);
	}

	private void saveAndSendNotification(NotificationEvent event, NotificationRequest notificationRequest) {
		event.getNotifications().forEach(notification -> {
			UserDTO user = userMgmtClient.getUserById(notification.getUserId());
			if (null != user.getToken()) {
				notificationRequest.setToken(user.getToken());
				notificationRequest.setTitle(notification.getTitle());
				notificationRequest.setBody(notification.getBody());
				sendNotification(notificationRequest);
			}
			saveNotification(notification, event.getNotificationEventType());
		});
	}

	private void saveNotification(final Notification notification, final NotificationEventType type) {
		NotificationModel notificationModel = new NotificationModel();
		notificationModel.setCreationTime(new Date());
		notificationModel.setMessage(notification.getBody());
		notificationModel.setTitle(notificationModel.getTitle());
		notificationModel.setType(type);
		notificationModel.setUserId(notification.getUserId());

		notificationService.save(notificationModel);
	}

	private void sendNotification(NotificationRequest notificationRequest) {
		try {
			fcmService.sendMessageToToken(notificationRequest);
		} catch (Exception e) {
			log.error("Exception on sendNotification ", e);
		}
	}
}
