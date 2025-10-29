package com.juvarya.order.client;


import com.hlt.commonservice.dto.StandardResponse;
import com.juvarya.order.dto.DeliveryPartnerDTO;
import com.juvarya.order.dto.OrderSummaryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "DELIVERYMANAGEMENT")
public interface DeliveryClient {

    @PostMapping("/api/delivery/api/partners/assign-partner")
    ResponseEntity<String> assignDelivery(@RequestBody OrderSummaryDTO order);


    @PutMapping("/api/delivery/api/partners/availability/{deliveryPartnerId}")
    StandardResponse<DeliveryPartnerDTO> updateAvailability(
            @PathVariable("deliveryPartnerId") String deliveryPartnerId,
            @RequestParam("available") Boolean available
    );
}
