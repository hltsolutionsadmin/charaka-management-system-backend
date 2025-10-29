package com.juvarya.order.client;

import com.juvarya.order.dto.ItemWiseOrderReportDTO;
import com.juvarya.order.dto.request.UpdateOrderStatusRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@FeignClient(name = "RESTAURANTMANAGEMENT")
public interface RestaurantFeignClient {

    @GetMapping("/api/restaurant/api/report/view/basic")
    Page<ItemWiseOrderReportDTO> viewBasicRestaurantSalesReport(
            @RequestParam(name = "businessId", required = false) Long businessId,
            @RequestParam(name = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(name = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    );

    @PutMapping("/api/restaurant/api/restaurantOrder/order/status")
    void updateOrderStatus(@RequestBody UpdateOrderStatusRequest request);


}

