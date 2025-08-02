package com.juvarya.delivery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaRepositories
@EnableScheduling
@EnableFeignClients
@ComponentScan(basePackages = {"com.juvarya.delivery", "com.juvarya.auth"})
public class Deliverymanagement {

    public static void main(String[] args) {
        SpringApplication.run(Deliverymanagement.class, args);
    }

}
