package com.juvarya.user.access.mgmt.utils;

import java.time.LocalTime;
import java.util.Set;

import com.juvarya.user.access.mgmt.model.ProductAttributeModel;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class BusinessTimingUtil {

	public static boolean isBusinessEnabled(Set<ProductAttributeModel> attributes) {
		String loginTimeStr = null;
		String logoutTimeStr = null;

		for (ProductAttributeModel attr : attributes) {
			if ("loginTime".equalsIgnoreCase(attr.getAttributeName())) {
				loginTimeStr = attr.getAttributeValue();
			} else if ("logoutTime".equalsIgnoreCase(attr.getAttributeName())) {
				logoutTimeStr = attr.getAttributeValue();
			}
		}

		if (loginTimeStr == null || logoutTimeStr == null) {
			return true;
		}

		try {
			LocalTime now = LocalTime.now();
			LocalTime loginTime = LocalTime.parse(loginTimeStr);
			LocalTime logoutTime = LocalTime.parse(logoutTimeStr);

			return !now.isBefore(loginTime) && !now.isAfter(logoutTime);
		} catch (Exception e) {

			return true;
		}
	}
}
