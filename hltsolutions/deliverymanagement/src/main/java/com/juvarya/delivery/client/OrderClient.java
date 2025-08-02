package com.juvarya.delivery.client;

import com.juvarya.commonservice.dto.ApiResponse;
import com.juvarya.delivery.dto.OrderDTO;
import com.juvarya.delivery.dto.enums.OrderStatus;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "ORDERMGMT")
public interface OrderClient {


    @PostMapping("/api/orders/status/{orderNumber}")
    ApiResponse<OrderDTO> updateOrderStatus(@PathVariable("orderNumber") String orderNumber,
                                            @RequestParam("status") OrderStatus status,
                                            @RequestParam(value = "notes", required = false) String notes,
                                            @RequestParam("updatedBy") String updatedBy);


    @GetMapping("/api/order/api/orders/delivery-partner-status/{deliveryPartnerId}")
    Boolean isDeliveryPartnerAssigned(@PathVariable("deliveryPartnerId") String deliveryPartnerId);
}





