package com.juvarya.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaRepositories
@EnableScheduling
@EnableFeignClients
@ComponentScan(basePackages = {"com.juvarya.order","com.juvarya.auth","com.juvarya.auth","firebase"})
public class OrderManagementService {

    public static void main(String[] args) {
        SpringApplication.run(OrderManagementService.class, args);
    }

}
