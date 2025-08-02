package com.juvarya.user.access.mgmt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
@ComponentScan(basePackages = {"com.juvarya.user.access.mgmt", "com.juvarya.auth", "firebase"})
public class 	UserManagement {

	public static void main(String[] args) {
		SpringApplication.run(UserManagement.class, args);
	}

}
