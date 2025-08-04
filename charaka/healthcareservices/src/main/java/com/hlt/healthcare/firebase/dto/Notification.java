package com.hlt.healthcare.firebase.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.auto.value.AutoValue.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
	private String title;
	private String body;
	private Long spaceId;
	private Long userId;

	@JsonIgnore
	public static Notification buildNotification(final Long userId, final String title, final String body,
			final Long spaceId) {
		Notification data = new Notification();
		data.setTitle(title);
		data.setBody(body);
		data.setUserId(userId);
		data.setSpaceId(spaceId);
		return data;
	}

	@JsonIgnore
	public static Notification buildNotification(final Long userId, final Long spaceId,
                                                 final NotificationEventType eventType, final Map<String, String> params) {
		Notification data = new Notification();
		data.setTitle(eventType.getTitle(params));
		data.setBody(eventType.getBody(params));
		data.setUserId(userId);
		if (spaceId != null) {
			data.setSpaceId(spaceId);
		}
		return data;
	}

}
