package com.hlt.productmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.ComponentScan;

@EnableAsync
@EnableScheduling
@EnableFeignClients
@SpringBootApplication
@ComponentScan(basePackages = {"com.hlt.productmanagement", "com.hlt.auth"})
public class ProductManagement {
    public static void main(String[] args) {
        SpringApplication.run(ProductManagement.class, args);
    }
}
